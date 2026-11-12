package com.equipmenthub.rentalservice.dto.request;

import com.equipmenthub.rentalservice.entity.ReservationItemStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationItemStatusRequest {

	@NotNull
	private ReservationItemStatus status;

	private List<UUID> unitIds;

	@Valid
	private List<UnitReturnInspectionRequest> unitInspections;

}
