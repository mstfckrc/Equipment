package com.equipmenthub.rentalservice.dto.response;

import java.util.UUID;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogListingResponse {

	private UUID productId;
	private UUID storeId;
	private String name;
	private BigDecimal dailyPrice;

}
