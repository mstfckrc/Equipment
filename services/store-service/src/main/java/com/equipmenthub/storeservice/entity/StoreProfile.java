package com.equipmenthub.storeservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

@Entity
@Table(name = "store_profile")
@Getter
@Setter
@NoArgsConstructor
public class StoreProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "owner_account_id", nullable = false, unique = true)
	private UUID ownerAccountId;

	@Column(name = "store_name", nullable = false, length = 200)
	private String storeName;

	@Column(nullable = false, length = 32)
	private String phone;

	@Column(nullable = false, length = 100)
	private String city;

	@Column(nullable = false, length = 100)
	private String district;

	@Column(name = "address_line", nullable = false, length = 500)
	private String addressLine;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@ColumnDefault("'ACTIVE'")
	private StoreStatus status = StoreStatus.ACTIVE;
}
