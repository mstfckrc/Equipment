package com.equipmenthub.rentalservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RentalInventoryRequest {

	@NotNull
	@Min(0)
	@Max(1000)
	private Integer totalUnits;

}
