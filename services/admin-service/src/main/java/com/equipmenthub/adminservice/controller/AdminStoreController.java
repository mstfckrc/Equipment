package com.equipmenthub.adminservice.controller;

import com.equipmenthub.adminservice.dto.request.StoreStatusUpdateRequest;
import com.equipmenthub.adminservice.dto.response.StoreProfileResponse;
import com.equipmenthub.adminservice.dto.response.StoreSummaryResponse;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/stores")
public class AdminStoreController {

	@Autowired
	private AdminService adminService;

	@GetMapping
	public List<StoreSummaryResponse> stores(@AuthenticationPrincipal Jwt jwt) {
		return adminService.getStores(jwt.getTokenValue());
	}

	@GetMapping("/{storeId}")
	public StoreProfileResponse store(@PathVariable UUID storeId, @AuthenticationPrincipal Jwt jwt) {
		return adminService.getStore(storeId, jwt.getTokenValue());
	}

	@PatchMapping("/{storeId}")
	public StoreProfileResponse updateStatus(
			@PathVariable UUID storeId,
			@Valid @RequestBody StoreStatusUpdateRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return adminService.updateStoreStatus(storeId, request, jwt.getTokenValue());
	}

}
