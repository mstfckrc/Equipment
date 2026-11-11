package com.equipmenthub.adminservice.controller;

import com.equipmenthub.adminservice.dto.request.ProductModerationRequest;
import com.equipmenthub.adminservice.dto.response.AdminProductResponse;
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
@RequestMapping("/api/admin/catalog/products")
public class AdminCatalogController {

	@Autowired
	private AdminService adminService;

	@GetMapping
	public List<AdminProductResponse> products(@AuthenticationPrincipal Jwt jwt) {
		return adminService.getCatalogProducts(jwt.getTokenValue());
	}

	@GetMapping("/{productId}")
	public AdminProductResponse product(@PathVariable UUID productId, @AuthenticationPrincipal Jwt jwt) {
		return adminService.getCatalogProduct(productId, jwt.getTokenValue());
	}

	@PatchMapping("/{productId}")
	public AdminProductResponse moderateProduct(
			@PathVariable UUID productId, @Valid @RequestBody ProductModerationRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return adminService.moderateCatalogProduct(productId, request, jwt.getTokenValue());
	}

}
