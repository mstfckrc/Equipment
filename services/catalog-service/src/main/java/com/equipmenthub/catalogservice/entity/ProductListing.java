package com.equipmenthub.catalogservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_listing")
@Getter
@Setter
@NoArgsConstructor
public class ProductListing {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "store_id", nullable = false)
	private UUID storeId;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(nullable = false, length = 100)
	private String category;

	@Column(nullable = false, length = 2000)
	private String description;

	@Column(name = "image_url", length = 1000)
	private String imageUrl;

	@Column(name = "daily_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal dailyPrice;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ProductStatus status = ProductStatus.DRAFT;

	@Column(name = "moderation_reason", length = 500)
	private String moderationReason;

	@Column(name = "moderated_by")
	private UUID moderatedBy;

	@Column(name = "moderated_at")
	private Instant moderatedAt;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
