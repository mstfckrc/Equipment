package com.equipmenthub.rentalservice.dto.request;

import com.equipmenthub.rentalservice.entity.EquipmentUnitStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitReturnInspectionRequest {

	@NotNull
	private UUID unitId;

	@NotNull
	private EquipmentUnitStatus condition;

	@Size(max = 500)
	private String damageNote;

	@DecimalMin(value = "0.00")
	@Digits(integer = 10, fraction = 2)
	private BigDecimal manualChargeAmount;

}
