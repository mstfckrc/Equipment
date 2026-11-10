package com.equipmenthub.authservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "store_registration")
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class StoreRegistration {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "auth_user_id", nullable = false, unique = true)
	private AuthUser user;

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

	@Column(name = "submitted_at", nullable = false)
	private Instant submittedAt;

	@Column(name = "reviewed_at")
	private Instant reviewedAt;

	@Column(name = "reviewed_by")
	private UUID reviewedBy;

	@Column(name = "rejection_reason", length = 500)
	private String rejectionReason;

	public StoreRegistration(AuthUser user, String storeName, String phone, String city,
			String district, String addressLine) {
		this.user = user;
		this.storeName = storeName.trim();
		this.phone = phone.trim();
		this.city = city.trim();
		this.district = district.trim();
		this.addressLine = addressLine.trim();
	}

	@PrePersist
	private void setSubmittedAt() {
		if (submittedAt == null) {
			submittedAt = Instant.now();
		}
	}

}
