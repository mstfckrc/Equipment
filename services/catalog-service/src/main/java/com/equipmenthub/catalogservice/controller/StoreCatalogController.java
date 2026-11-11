package com.equipmenthub.catalogservice.controller;

import com.equipmenthub.catalogservice.dto.request.OwnerProductStatusRequest;
import com.equipmenthub.catalogservice.dto.request.ProductListingRequest;
import com.equipmenthub.catalogservice.dto.response.ProductListingResponse;
import com.equipmenthub.catalogservice.service.ProductListingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/catalog/stores/me/products")
public class StoreCatalogController {

	@Autowired
	private ProductListingService productListingService;

	@GetMapping
	public List<ProductListingResponse> products(@AuthenticationPrincipal Jwt jwt) {
		return productListingService.getOwnProducts(jwt.getTokenValue());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ProductListingResponse create(
			@Valid @RequestBody ProductListingRequest request, @AuthenticationPrincipal Jwt jwt) {
		return productListingService.createProduct(request, jwt.getTokenValue());
	}

	@GetMapping("/{productId}")
	public ProductListingResponse product(@PathVariable UUID productId, @AuthenticationPrincipal Jwt jwt) {
		return productListingService.getOwnProduct(productId, jwt.getTokenValue());
	}

	@PutMapping("/{productId}")
	public ProductListingResponse update(
			@PathVariable UUID productId, @Valid @RequestBody ProductListingRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return productListingService.updateProduct(productId, request, jwt.getTokenValue());
	}

	@PatchMapping("/{productId}")
	public ProductListingResponse updateStatus(
			@PathVariable UUID productId, @Valid @RequestBody OwnerProductStatusRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return productListingService.updateOwnStatus(productId, request, jwt.getTokenValue());
	}

}
