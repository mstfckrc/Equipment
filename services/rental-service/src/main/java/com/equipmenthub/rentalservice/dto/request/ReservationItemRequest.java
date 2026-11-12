package com.equipmenthub.rentalservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationItemRequest {

	@NotNull
	private UUID storeId;

	@NotNull
	private UUID listingId;

	@Min(1)
	@Max(1000)
	private int quantity;

	@NotNull
	private LocalDate pickupDate;

	@NotNull
	private LocalDate returnDate;

	@NotNull
	@DecimalMin("0.01")
	@Digits(integer = 8, fraction = 2)
	private BigDecimal expectedDailyPrice;

}
