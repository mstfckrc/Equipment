package com.equipmenthub.adminservice.controller;

import java.util.List;
import java.util.UUID;

import com.equipmenthub.adminservice.dto.request.StoreRegistrationReviewRequest;
import com.equipmenthub.adminservice.dto.response.StoreRegistrationResponse;
import com.equipmenthub.adminservice.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/store-registrations")
@RequiredArgsConstructor
public class AdminController {

	private final AdminService adminService;

	@GetMapping
	public List<StoreRegistrationResponse> pendingStoreRegistrations(@AuthenticationPrincipal Jwt jwt) {
		return adminService.getPendingStoreRegistrations(jwt.getTokenValue());
	}

	@PostMapping("/{registrationId}/approve")
	public StoreRegistrationResponse approveStoreRegistration(
			@PathVariable UUID registrationId, @AuthenticationPrincipal Jwt jwt) {
		return adminService.approveStoreRegistration(registrationId, jwt.getTokenValue());
	}

	@PostMapping("/{registrationId}/reject")
	public StoreRegistrationResponse rejectStoreRegistration(
			@PathVariable UUID registrationId,
			@Valid @RequestBody StoreRegistrationReviewRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return adminService.rejectStoreRegistration(registrationId, request, jwt.getTokenValue());
	}

}
