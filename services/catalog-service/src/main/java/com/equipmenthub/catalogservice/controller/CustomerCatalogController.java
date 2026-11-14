package com.equipmenthub.catalogservice.controller;

import com.equipmenthub.catalogservice.dto.response.PublicProductResponse;
import com.equipmenthub.catalogservice.service.ProductListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/catalog/stores/{storeId}/products")
public class CustomerCatalogController {

	@Autowired
	private ProductListingService productListingService;

	@GetMapping
	public List<PublicProductResponse> products(@PathVariable UUID storeId) {
		return productListingService.getStoreProducts(storeId);
	}

	@GetMapping("/{productId}")
	public PublicProductResponse product(@PathVariable UUID storeId, @PathVariable UUID productId) {
		return productListingService.getStoreProduct(storeId, productId);
	}

}
