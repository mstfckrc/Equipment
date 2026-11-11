package com.equipmenthub.catalogservice.service;

import com.equipmenthub.catalogservice.client.StoreServiceClient;
import com.equipmenthub.catalogservice.dto.request.OwnerProductStatusRequest;
import com.equipmenthub.catalogservice.dto.request.ProductListingRequest;
import com.equipmenthub.catalogservice.dto.request.ProductStatusUpdateRequest;
import com.equipmenthub.catalogservice.dto.response.ProductListingResponse;
import com.equipmenthub.catalogservice.dto.response.PublicProductResponse;
import com.equipmenthub.catalogservice.entity.ProductListing;
import com.equipmenthub.catalogservice.entity.ProductStatus;
import com.equipmenthub.catalogservice.exception.CatalogException;
import com.equipmenthub.catalogservice.repository.ProductListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ProductListingService {

	@Autowired
	private ProductListingRepository productListingRepository;

	@Autowired
	private StoreServiceClient storeServiceClient;

	public List<PublicProductResponse> getStoreProducts(UUID storeId, String accessToken) {
		storeServiceClient.requireActiveStore(storeId, accessToken);
		return productListingRepository.findAllByStoreIdAndStatusOrderByNameAsc(storeId, ProductStatus.PUBLISHED)
				.stream().map(PublicProductResponse::from).toList();
	}

	public PublicProductResponse getStoreProduct(UUID storeId, UUID productId, String accessToken) {
		storeServiceClient.requireActiveStore(storeId, accessToken);
		return productListingRepository.findByIdAndStoreIdAndStatus(productId, storeId, ProductStatus.PUBLISHED)
				.map(PublicProductResponse::from)
				.orElseThrow(() -> new CatalogException(HttpStatus.NOT_FOUND, "Product not found"));
	}

	public List<ProductListingResponse> getOwnProducts(String accessToken) {
		UUID storeId = storeServiceClient.getActiveStoreId(accessToken);
		return productListingRepository.findAllByStoreIdOrderByCreatedAtDesc(storeId)
				.stream().map(ProductListingResponse::from).toList();
	}

	public ProductListingResponse createProduct(ProductListingRequest request, String accessToken) {
		UUID storeId = storeServiceClient.getActiveStoreId(accessToken);
		ProductListing product = new ProductListing();
		product.setStoreId(storeId);
		product.setStatus(ProductStatus.DRAFT);
		applyDetails(product, request);
		return ProductListingResponse.from(productListingRepository.save(product));
	}

	public ProductListingResponse getOwnProduct(UUID productId, String accessToken) {
		UUID storeId = storeServiceClient.getActiveStoreId(accessToken);
		return findOwnedProduct(productId, storeId);
	}

	public ProductListingResponse updateProduct(UUID productId, ProductListingRequest request, String accessToken) {
		UUID storeId = storeServiceClient.getActiveStoreId(accessToken);
		ProductListing product = findEntity(productId, storeId);
		applyDetails(product, request);
		return ProductListingResponse.from(productListingRepository.save(product));
	}

	public ProductListingResponse updateOwnStatus(
			UUID productId, OwnerProductStatusRequest request, String accessToken) {
		if (request.getStatus() == ProductStatus.SUSPENDED) {
			throw new CatalogException(HttpStatus.BAD_REQUEST, "Only an administrator can suspend a product");
		}
		UUID storeId = storeServiceClient.getActiveStoreId(accessToken);
		ProductListing product = findEntity(productId, storeId);
		if (product.getStatus() == ProductStatus.SUSPENDED) {
			throw new CatalogException(HttpStatus.FORBIDDEN, "A suspended product can only be restored by an administrator");
		}
		product.setStatus(request.getStatus());
		return ProductListingResponse.from(productListingRepository.save(product));
	}

	@Transactional(readOnly = true)
	public List<ProductListingResponse> getAllProducts() {
		return productListingRepository.findAllByOrderByCreatedAtDesc().stream()
				.map(ProductListingResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public ProductListingResponse getAdminProduct(UUID productId) {
		return productListingRepository.findById(productId).map(ProductListingResponse::from)
				.orElseThrow(() -> new CatalogException(HttpStatus.NOT_FOUND, "Product not found"));
	}

	@Transactional
	public ProductListingResponse updateAdminStatus(
			UUID productId, ProductStatusUpdateRequest request, UUID adminId) {
		if (request.getStatus() != ProductStatus.SUSPENDED && request.getStatus() != ProductStatus.DRAFT) {
			throw new CatalogException(HttpStatus.BAD_REQUEST,
					"Administrator can suspend a product or return it to draft");
		}
		if (request.getStatus() == ProductStatus.SUSPENDED
				&& (request.getReason() == null || request.getReason().isBlank())) {
			throw new CatalogException(HttpStatus.BAD_REQUEST, "A reason is required when suspending a product");
		}
		ProductListing product = productListingRepository.findById(productId)
				.orElseThrow(() -> new CatalogException(HttpStatus.NOT_FOUND, "Product not found"));
		product.setStatus(request.getStatus());
		product.setModerationReason(request.getReason() == null || request.getReason().isBlank()
				? null : request.getReason().trim());
		product.setModeratedBy(adminId);
		product.setModeratedAt(Instant.now());
		return ProductListingResponse.from(productListingRepository.save(product));
	}

	private ProductListingResponse findOwnedProduct(UUID productId, UUID storeId) {
		return productListingRepository.findByIdAndStoreId(productId, storeId)
				.map(ProductListingResponse::from)
				.orElseThrow(() -> new CatalogException(HttpStatus.NOT_FOUND, "Product not found"));
	}

	private ProductListing findEntity(UUID productId, UUID storeId) {
		return productListingRepository.findByIdAndStoreId(productId, storeId)
				.orElseThrow(() -> new CatalogException(HttpStatus.NOT_FOUND, "Product not found"));
	}

	private void applyDetails(ProductListing product, ProductListingRequest request) {
		product.setName(request.getName().trim());
		product.setCategory(request.getCategory().trim());
		product.setDescription(request.getDescription().trim());
		String imageUrl = request.getImageUrl();
		product.setImageUrl(imageUrl == null || imageUrl.isBlank() ? null : imageUrl.trim());
		product.setDailyPrice(request.getDailyPrice());
	}

}
