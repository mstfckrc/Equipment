package com.equipmenthub.catalogservice.dto.response;

import com.equipmenthub.catalogservice.entity.ProductListing;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicProductResponse {

	private UUID productId;
	private UUID storeId;
	private String name;
	private String category;
	private String description;
	private String imageUrl;
	private BigDecimal dailyPrice;

	public static PublicProductResponse from(ProductListing product) {
		return PublicProductResponse.builder()
				.productId(product.getId())
				.storeId(product.getStoreId())
				.name(product.getName())
				.category(product.getCategory())
				.description(product.getDescription())
				.imageUrl(product.getImageUrl())
				.dailyPrice(product.getDailyPrice())
				.build();
	}

}
