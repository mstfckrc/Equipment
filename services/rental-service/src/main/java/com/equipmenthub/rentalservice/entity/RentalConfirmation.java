package com.equipmenthub.rentalservice.entity;

import com.equipmenthub.rentalservice.dto.response.ConfirmationContent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rental_confirmation")
@Getter
@Setter
@NoArgsConstructor
public class RentalConfirmation {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "checkout_id", nullable = false, unique = true)
	private RentalCheckout checkout;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ConfirmationStatus status = ConfirmationStatus.PENDING;

	@Column(name = "content_version", nullable = false)
	private int contentVersion = 1;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private ConfirmationContent content;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "prepared_at")
	private Instant preparedAt;

	@Column(name = "error_code", length = 60)
	private String errorCode;

}
