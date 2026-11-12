package com.equipmenthub.rentalservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityConflictResponse {

	private int itemIndex;
	private UUID storeId;
	private UUID listingId;
	private LocalDate date;
	private int requestedQuantity;
	private int remainingQuantity;

}
