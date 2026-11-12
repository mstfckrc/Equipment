package com.equipmenthub.rentalservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalInventorySummaryResponse {

	private UUID listingId;
	private long totalUnits;
	private long readyUnits;
	private long maintenanceUnits;
	private long retiredUnits;
	private long rentedUnits;
	private long pendingInspectionUnits;

}
