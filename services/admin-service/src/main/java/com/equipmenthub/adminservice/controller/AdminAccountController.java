package com.equipmenthub.adminservice.controller;

import com.equipmenthub.adminservice.dto.request.AccountEnabledUpdateRequest;
import com.equipmenthub.adminservice.dto.response.AccountSummaryResponse;
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
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {

	@Autowired
	private AdminService adminService;

	@GetMapping
	public List<AccountSummaryResponse> accounts(@AuthenticationPrincipal Jwt jwt) {
		return adminService.getAccounts(jwt.getTokenValue());
	}

	@PatchMapping("/{accountId}")
	public AccountSummaryResponse updateAccountEnabled(
			@PathVariable UUID accountId,
			@Valid @RequestBody AccountEnabledUpdateRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return adminService.updateAccountEnabled(accountId, request, jwt.getTokenValue());
	}

}
