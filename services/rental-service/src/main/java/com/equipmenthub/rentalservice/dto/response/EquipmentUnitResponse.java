package com.equipmenthub.rentalservice.dto.response;

import com.equipmenthub.rentalservice.entity.EquipmentUnit;
import com.equipmenthub.rentalservice.entity.EquipmentUnitStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentUnitResponse {

	private UUID unitId;
	private String unitCode;
	private EquipmentUnitStatus status;
	private Instant createdAt;
	private Instant updatedAt;

	public static EquipmentUnitResponse from(EquipmentUnit unit) {
		return EquipmentUnitResponse.builder()
				.unitId(unit.getId())
				.unitCode(unit.getUnitCode())
				.status(unit.getStatus())
				.createdAt(unit.getCreatedAt())
				.updatedAt(unit.getUpdatedAt())
				.build();
	}

}
