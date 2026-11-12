package com.equipmenthub.rentalservice.dto.response;

import com.equipmenthub.rentalservice.entity.EquipmentUnitStatus;
import com.equipmenthub.rentalservice.entity.ReservationUnitAssignment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationUnitAssignmentResponse {

	private UUID unitId;
	private String unitCode;
	private EquipmentUnitStatus equipmentStatus;
	private Instant handedOverAt;
	private Instant receivedAt;
	private Instant inspectedAt;
	private EquipmentUnitStatus returnCondition;
	private String damageNote;
	private BigDecimal manualChargeAmount;
	private String currencyCode;

	public static ReservationUnitAssignmentResponse from(ReservationUnitAssignment assignment) {
		return ReservationUnitAssignmentResponse.builder()
				.unitId(assignment.getEquipmentUnit().getId())
				.unitCode(assignment.getEquipmentUnit().getUnitCode())
				.equipmentStatus(assignment.getEquipmentUnit().getStatus())
				.handedOverAt(assignment.getHandedOverAt())
				.receivedAt(assignment.getReceivedAt())
				.inspectedAt(assignment.getInspectedAt())
				.returnCondition(assignment.getReturnCondition())
				.damageNote(assignment.getDamageNote())
				.manualChargeAmount(assignment.getManualChargeAmount())
				.currencyCode(assignment.getCurrencyCode())
				.build();
	}
}
