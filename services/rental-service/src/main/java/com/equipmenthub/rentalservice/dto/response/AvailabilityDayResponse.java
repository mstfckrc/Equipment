package com.equipmenthub.rentalservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityDayResponse {

	private LocalDate date;
	private int readyUnits;
	private int reservedUnits;
	private int remainingUnits;

}
