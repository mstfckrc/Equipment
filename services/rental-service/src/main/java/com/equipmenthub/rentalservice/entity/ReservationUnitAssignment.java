package com.equipmenthub.rentalservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservation_unit_assignment", uniqueConstraints = {
		@UniqueConstraint(name = "uk_reservation_unit_assignment_item_unit",
				columnNames = { "reservation_item_id", "equipment_unit_id" })
})
@Getter
@Setter
@NoArgsConstructor
public class ReservationUnitAssignment {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "reservation_item_id", nullable = false)
	private ReservationItem reservationItem;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "equipment_unit_id", nullable = false)
	private EquipmentUnit equipmentUnit;

	@Column(name = "handed_over_at", nullable = false)
	private Instant handedOverAt;

	@Column(name = "received_at")
	private Instant receivedAt;

	@Column(name = "inspected_at")
	private Instant inspectedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "return_condition", length = 20)
	private EquipmentUnitStatus returnCondition;

	@Column(name = "damage_note", length = 500)
	private String damageNote;

	@Column(name = "manual_charge_amount", precision = 12, scale = 2)
	private BigDecimal manualChargeAmount;

	@Column(name = "currency_code", nullable = false, length = 3)
	private String currencyCode = "TRY";

}
