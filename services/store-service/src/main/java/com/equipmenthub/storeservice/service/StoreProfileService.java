package com.equipmenthub.storeservice.service;

import com.equipmenthub.storeservice.dto.request.StoreProfileProvisionRequest;
import com.equipmenthub.storeservice.dto.request.StoreProfileUpdateRequest;
import com.equipmenthub.storeservice.dto.response.StoreOwnershipResponse;
import com.equipmenthub.storeservice.dto.response.StoreProfileResponse;
import com.equipmenthub.storeservice.dto.response.PublicStoreResponse;
import com.equipmenthub.storeservice.dto.response.PublicStoreSummaryResponse;
import com.equipmenthub.storeservice.dto.response.StoreSummaryResponse;
import com.equipmenthub.storeservice.entity.StoreProfile;
import com.equipmenthub.storeservice.entity.StoreStatus;
import com.equipmenthub.storeservice.exception.StoreException;
import com.equipmenthub.storeservice.repository.StoreProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class StoreProfileService {

	@Autowired
	private StoreProfileRepository storeProfileRepository;

	@Transactional(readOnly = true)
	public StoreProfileResponse getOwnProfile(UUID accountId) {
		return StoreProfileResponse.from(findByOwnerAccountId(accountId));
	}

	@Transactional
	public StoreProfileResponse updateOwnProfile(UUID accountId, StoreProfileUpdateRequest request) {
		StoreProfile profile = findByOwnerAccountId(accountId);
		profile.setStoreName(request.getStoreName().trim());
		profile.setPhone(request.getPhone().trim());
		profile.setCity(request.getCity().trim());
		profile.setDistrict(request.getDistrict().trim());
		profile.setAddressLine(request.getAddressLine().trim());
		return StoreProfileResponse.from(storeProfileRepository.save(profile));
	}

	@Transactional(readOnly = true)
	public StoreOwnershipResponse resolveActiveStore(UUID accountId) {
		StoreProfile profile = findByOwnerAccountId(accountId);
		if (profile.getStatus() != StoreStatus.ACTIVE) {
			throw new StoreException(HttpStatus.FORBIDDEN, "This store is currently suspended");
		}
		return StoreOwnershipResponse.builder().storeId(profile.getId()).build();
	}

	@Transactional(readOnly = true)
	public List<StoreSummaryResponse> getStores() {
		return storeProfileRepository.findAll().stream().map(StoreSummaryResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public List<PublicStoreSummaryResponse> getPublicStores(List<UUID> approvedStoreIds) {
		if (approvedStoreIds.isEmpty()) {
			return List.of();
		}
		return storeProfileRepository.findAllByIdInAndStatusOrderByStoreNameAsc(approvedStoreIds, StoreStatus.ACTIVE)
				.stream().map(PublicStoreSummaryResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public PublicStoreResponse getPublicStore(UUID storeId, List<UUID> approvedStoreIds) {
		if (!approvedStoreIds.contains(storeId)) {
			throw new StoreException(HttpStatus.NOT_FOUND, "Store not found");
		}
		return storeProfileRepository.findByIdAndStatus(storeId, StoreStatus.ACTIVE)
				.map(PublicStoreResponse::from)
				.orElseThrow(() -> new StoreException(HttpStatus.NOT_FOUND, "Store not found"));
	}

	@Transactional(readOnly = true)
	public StoreProfileResponse getStore(UUID storeId) {
		return StoreProfileResponse.from(findById(storeId));
	}

	@Transactional
	public StoreProfileResponse updateStoreStatus(UUID storeId, StoreStatus status) {
		StoreProfile profile = findById(storeId);
		profile.setStatus(status);
		return StoreProfileResponse.from(storeProfileRepository.save(profile));
	}

	@Transactional
	public StoreProfileResponse provision(StoreProfileProvisionRequest request) {
		StoreProfile existing = storeProfileRepository.findByOwnerAccountId(request.getAccountId()).orElse(null);
		if (existing != null) {
			if (!sameProfile(existing, request)) {
				throw new StoreException(HttpStatus.CONFLICT,
						"Store registration details conflict with an existing profile");
			}
			return StoreProfileResponse.from(existing);
		}
		StoreProfile profile = new StoreProfile();
		profile.setOwnerAccountId(request.getAccountId());
		profile.setStoreName(request.getStoreName().trim());
		profile.setPhone(request.getPhone().trim());
		profile.setCity(request.getCity().trim());
		profile.setDistrict(request.getDistrict().trim());
		profile.setAddressLine(request.getAddressLine().trim());
		profile.setStatus(StoreStatus.ACTIVE);
		return StoreProfileResponse.from(storeProfileRepository.save(profile));
	}

	private boolean sameProfile(StoreProfile profile, StoreProfileProvisionRequest request) {
		return profile.getStoreName().equals(request.getStoreName().trim())
				&& profile.getPhone().equals(request.getPhone().trim())
				&& profile.getCity().equals(request.getCity().trim())
				&& profile.getDistrict().equals(request.getDistrict().trim())
				&& profile.getAddressLine().equals(request.getAddressLine().trim());
	}

	private StoreProfile findByOwnerAccountId(UUID accountId) {
		return storeProfileRepository.findByOwnerAccountId(accountId)
				.orElseThrow(() -> new StoreException(HttpStatus.NOT_FOUND, "Store profile not found"));
	}

	private StoreProfile findById(UUID storeId) {
		return storeProfileRepository.findById(storeId)
				.orElseThrow(() -> new StoreException(HttpStatus.NOT_FOUND, "Store not found"));
	}

}
