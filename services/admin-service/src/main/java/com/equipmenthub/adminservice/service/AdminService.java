package com.equipmenthub.adminservice.service;

import java.util.List;
import java.util.UUID;

import com.equipmenthub.adminservice.client.AuthServiceClient;
import com.equipmenthub.adminservice.client.CatalogServiceClient;
import com.equipmenthub.adminservice.client.StoreServiceClient;
import com.equipmenthub.adminservice.dto.request.StoreRegistrationReviewRequest;
import com.equipmenthub.adminservice.dto.request.StoreStatusUpdateRequest;
import com.equipmenthub.adminservice.dto.request.ProductModerationRequest;
import com.equipmenthub.adminservice.dto.response.AdminProductResponse;
import com.equipmenthub.adminservice.dto.response.StoreProfileResponse;
import com.equipmenthub.adminservice.dto.response.StoreRegistrationResponse;
import com.equipmenthub.adminservice.dto.response.StoreRegistrationSummaryResponse;
import com.equipmenthub.adminservice.dto.response.StoreSummaryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

	@Autowired
	private AuthServiceClient authServiceClient;

	@Autowired
	private StoreServiceClient storeServiceClient;

	@Autowired
	private CatalogServiceClient catalogServiceClient;

	public List<StoreRegistrationSummaryResponse> getStoreRegistrations(String accessToken) {
		return authServiceClient.getStoreRegistrations(accessToken);
	}

	public StoreRegistrationResponse getStoreRegistration(UUID registrationId, String accessToken) {
		return authServiceClient.getStoreRegistration(registrationId, accessToken);
	}

	public StoreRegistrationResponse reviewStoreRegistration(
			UUID registrationId, StoreRegistrationReviewRequest request, String accessToken) {
		return authServiceClient.reviewStoreRegistration(registrationId, request, accessToken);
	}

	public List<StoreSummaryResponse> getStores(String accessToken) {
		return storeServiceClient.getStores(accessToken);
	}

	public StoreProfileResponse getStore(UUID storeId, String accessToken) {
		return storeServiceClient.getStore(storeId, accessToken);
	}

	public StoreProfileResponse updateStoreStatus(
			UUID storeId, StoreStatusUpdateRequest request, String accessToken) {
		return storeServiceClient.updateStoreStatus(storeId, request, accessToken);
	}

	public List<AdminProductResponse> getCatalogProducts(String accessToken) {
		return catalogServiceClient.getProducts(accessToken);
	}

	public AdminProductResponse getCatalogProduct(UUID productId, String accessToken) {
		return catalogServiceClient.getProduct(productId, accessToken);
	}

	public AdminProductResponse moderateCatalogProduct(
			UUID productId, ProductModerationRequest request, String accessToken) {
		return catalogServiceClient.updateStatus(productId, request, accessToken);
	}

}
