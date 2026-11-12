package com.equipmenthub.rentalservice.dto.request;

import com.equipmenthub.rentalservice.entity.EquipmentUnitStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EquipmentUnitStatusRequest {

	@NotNull
	private EquipmentUnitStatus status;

}
