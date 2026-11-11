package com.equipmenthub.catalogservice.controller;

import com.equipmenthub.catalogservice.dto.request.ProductStatusUpdateRequest;
import com.equipmenthub.catalogservice.dto.response.ProductListingResponse;
import com.equipmenthub.catalogservice.service.ProductListingService;
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
@RequestMapping("/internal/admin/products")
public class InternalAdminCatalogController {

	@Autowired
	private ProductListingService productListingService;

	@GetMapping
	public List<ProductListingResponse> products() {
		return productListingService.getAllProducts();
	}

	@GetMapping("/{productId}")
	public ProductListingResponse product(@PathVariable UUID productId) {
		return productListingService.getAdminProduct(productId);
	}

	@PatchMapping("/{productId}")
	public ProductListingResponse updateStatus(
			@PathVariable UUID productId, @Valid @RequestBody ProductStatusUpdateRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return productListingService.updateAdminStatus(productId, request, UUID.fromString(jwt.getSubject()));
	}

}
