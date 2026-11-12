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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "equipment_unit", uniqueConstraints = {
		@UniqueConstraint(name = "uk_equipment_unit_inventory_number", columnNames = { "inventory_id", "unit_number" }),
		@UniqueConstraint(name = "uk_equipment_unit_inventory_code", columnNames = { "inventory_id", "unit_code" })
})
@Getter
@Setter
@NoArgsConstructor
public class EquipmentUnit {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "inventory_id", nullable = false)
	private RentalInventory inventory;

	@Column(name = "unit_number", nullable = false)
	private long unitNumber;

	@Column(name = "unit_code", nullable = false, length = 20)
	private String unitCode;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private EquipmentUnitStatus status = EquipmentUnitStatus.READY;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

}
