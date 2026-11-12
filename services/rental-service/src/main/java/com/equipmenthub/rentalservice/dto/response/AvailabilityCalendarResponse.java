package com.equipmenthub.rentalservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityCalendarResponse {

	private UUID storeId;
	private UUID listingId;
	private LocalDate fromDate;
	private LocalDate toDateExclusive;
	private String currencyCode;
	private String listingName;
	private java.math.BigDecimal dailyPrice;
	private List<AvailabilityDayResponse> days;

}
