package com.equipmenthub.storeservice.controller;

import com.equipmenthub.storeservice.dto.request.StoreProfileProvisionRequest;
import com.equipmenthub.storeservice.dto.request.StoreProfileUpdateRequest;
import com.equipmenthub.storeservice.dto.response.StoreOwnershipResponse;
import com.equipmenthub.storeservice.dto.response.StoreProfileResponse;
import com.equipmenthub.storeservice.dto.response.PublicStoreResponse;
import com.equipmenthub.storeservice.dto.response.PublicStoreSummaryResponse;
import com.equipmenthub.storeservice.client.AuthServiceClient;
import com.equipmenthub.storeservice.service.StoreProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.List;


@RestController
@RequestMapping("/api/stores")
public class StoreProfileController {

	@Autowired
	private StoreProfileService storeProfileService;

	@Autowired
	private AuthServiceClient authServiceClient;

	@GetMapping
	public List<PublicStoreSummaryResponse> getPublicStores(@AuthenticationPrincipal Jwt jwt) {
		return storeProfileService.getPublicStores(authServiceClient.getApprovedStoreIds(jwt.getTokenValue()));
	}

	@GetMapping("/{storeId}")
	public PublicStoreResponse getPublicStore(@PathVariable UUID storeId, @AuthenticationPrincipal Jwt jwt) {
		return storeProfileService.getPublicStore(
				storeId, authServiceClient.getApprovedStoreIds(jwt.getTokenValue()));
	}

	@GetMapping("/me")
	public StoreProfileResponse getOwnProfile(@AuthenticationPrincipal Jwt jwt) {
		return storeProfileService.getOwnProfile(UUID.fromString(jwt.getSubject()));
	}

	@PutMapping("/me")
	public StoreProfileResponse updateOwnProfile(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody StoreProfileUpdateRequest request) {
		return storeProfileService.updateOwnProfile(UUID.fromString(jwt.getSubject()), request);
	}

	@GetMapping("/internal/ownership")
	public StoreOwnershipResponse resolveActiveStore(@AuthenticationPrincipal Jwt jwt) {
		return storeProfileService.resolveActiveStore(UUID.fromString(jwt.getSubject()));
	}

	@PostMapping("/internal/profiles")
	@ResponseStatus(HttpStatus.CREATED)
	public StoreProfileResponse provision(@Valid @RequestBody StoreProfileProvisionRequest request) {
		return storeProfileService.provision(request);
	}
}
