package com.equipmenthub.rentalservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {

	private String code;
	private String message;
	private java.util.List<PriceChangeResponse> priceChanges;
	private java.util.List<AvailabilityConflictResponse> availabilityConflicts;

	public ApiErrorResponse(String message) {
		this("ERROR", message, java.util.List.of(), java.util.List.of());
	}

}
