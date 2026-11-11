package com.equipmenthub.adminservice.dto.response;

import com.equipmenthub.adminservice.dto.request.AdminProductStatus;
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
public class AdminProductResponse {

	private UUID productId;
	private UUID storeId;
	private String name;
	private String category;
	private String description;
	private String imageUrl;
	private BigDecimal dailyPrice;
	private AdminProductStatus status;
	private String moderationReason;
	private UUID moderatedBy;
	private Instant createdAt;
	private Instant updatedAt;

}
