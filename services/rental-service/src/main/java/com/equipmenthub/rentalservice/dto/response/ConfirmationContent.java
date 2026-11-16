package com.equipmenthub.rentalservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmationContent {

	private UUID checkoutId;
	private Instant reservedAt;
	private Instant preparedAt;
	private String timeZone;
	private String currencyCode;
	private BigDecimal totalAmount;
	private List<StoreSection> stores;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StoreSection {
		private UUID storeId;
		private UUID reservationId;
		private String storeName;
		private BigDecimal totalAmount;
		private List<Line> items;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Line {
		private UUID itemId;
		private UUID listingId;
		private String listingName;
		private int quantity;
		private OffsetDateTime pickupAt;
		private OffsetDateTime returnAt;
		private long rentalDays;
		private BigDecimal dailyPrice;
		private BigDecimal totalAmount;
	}

}
