package com.equipmenthub.rentalservice.dto.response;

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
public class PriceChangeResponse {

	private int itemIndex;
	private UUID storeId;
	private UUID listingId;
	private String listingName;
	private BigDecimal previouslyShownDailyPrice;
	private BigDecimal previouslyShownLineTotal;
	private BigDecimal currentDailyPrice;
	private BigDecimal currentLineTotal;
	private String currencyCode;

}
