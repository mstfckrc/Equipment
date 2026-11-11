package com.equipmenthub.catalogservice.dto.response;

import com.equipmenthub.catalogservice.entity.ProductListing;
import com.equipmenthub.catalogservice.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListingResponse {

	private UUID productId;
	private UUID storeId;
	private String name;
	private String category;
	private String description;
	private String imageUrl;
	private BigDecimal dailyPrice;
	private ProductStatus status;
	private String moderationReason;
	private UUID moderatedBy;
	private Instant createdAt;
	private Instant updatedAt;

	public static ProductListingResponse from(ProductListing product) {
		return ProductListingResponse.builder()
				.productId(product.getId())
				.storeId(product.getStoreId())
				.name(product.getName())
				.category(product.getCategory())
				.description(product.getDescription())
				.imageUrl(product.getImageUrl())
				.dailyPrice(product.getDailyPrice())
				.status(product.getStatus())
				.moderationReason(product.getModerationReason())
				.moderatedBy(product.getModeratedBy())
				.createdAt(product.getCreatedAt())
				.updatedAt(product.getUpdatedAt())
				.build();
	}

}
