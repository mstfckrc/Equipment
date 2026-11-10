package com.equipmenthub.adminservice.service;

import java.util.List;
import java.util.UUID;

import com.equipmenthub.adminservice.client.AuthServiceClient;
import com.equipmenthub.adminservice.dto.request.StoreRegistrationReviewRequest;
import com.equipmenthub.adminservice.dto.response.StoreRegistrationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

	private final AuthServiceClient authServiceClient;

	public List<StoreRegistrationResponse> getPendingStoreRegistrations(String accessToken) {
		return authServiceClient.getPendingStoreRegistrations(accessToken);
	}

	public StoreRegistrationResponse approveStoreRegistration(UUID registrationId, String accessToken) {
		return authServiceClient.approveStoreRegistration(registrationId, accessToken);
	}

	public StoreRegistrationResponse rejectStoreRegistration(
			UUID registrationId, StoreRegistrationReviewRequest request, String accessToken) {
		return authServiceClient.rejectStoreRegistration(registrationId, request, accessToken);
	}

}
