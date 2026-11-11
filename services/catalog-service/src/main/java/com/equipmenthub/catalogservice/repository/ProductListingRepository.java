package com.equipmenthub.catalogservice.repository;

import com.equipmenthub.catalogservice.entity.ProductListing;
import com.equipmenthub.catalogservice.entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductListingRepository extends JpaRepository<ProductListing, UUID> {

	List<ProductListing> findAllByStoreIdOrderByCreatedAtDesc(UUID storeId);

	List<ProductListing> findAllByStoreIdAndStatusOrderByNameAsc(UUID storeId, ProductStatus status);

	List<ProductListing> findAllByOrderByCreatedAtDesc();

	Optional<ProductListing> findByIdAndStoreId(UUID id, UUID storeId);

	Optional<ProductListing> findByIdAndStoreIdAndStatus(UUID id, UUID storeId, ProductStatus status);

}
