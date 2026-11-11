package com.equipmenthub.adminservice.controller;

import java.util.List;
import java.util.UUID;

import com.equipmenthub.adminservice.dto.request.StoreRegistrationReviewRequest;
import com.equipmenthub.adminservice.dto.response.StoreRegistrationResponse;
import com.equipmenthub.adminservice.dto.response.StoreRegistrationSummaryResponse;
import com.equipmenthub.adminservice.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/store-registrations")
public class AdminController {

	@Autowired
	private AdminService adminService;

	@GetMapping
	public List<StoreRegistrationSummaryResponse> storeRegistrations(@AuthenticationPrincipal Jwt jwt) {
		return adminService.getStoreRegistrations(jwt.getTokenValue());
	}

	@GetMapping("/{registrationId}")
	public StoreRegistrationResponse storeRegistration(
			@PathVariable UUID registrationId, @AuthenticationPrincipal Jwt jwt) {
		return adminService.getStoreRegistration(registrationId, jwt.getTokenValue());
	}

	@PatchMapping("/{registrationId}")
	public StoreRegistrationResponse reviewStoreRegistration(
			@PathVariable UUID registrationId,
			@Valid @RequestBody StoreRegistrationReviewRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return adminService.reviewStoreRegistration(registrationId, request, jwt.getTokenValue());
	}

}
