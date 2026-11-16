package com.equipmenthub.rentalservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rental_outbox_event", indexes = {
		@Index(name = "idx_rental_outbox_due", columnList = "published_at,next_attempt_at")
})
@Getter
@Setter
@NoArgsConstructor
public class RentalOutboxEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "checkout_id", nullable = false, unique = true)
	private RentalCheckout checkout;

	@Column(name = "occurred_at", nullable = false, updatable = false)
	private Instant occurredAt;

	@Column(name = "published_at")
	private Instant publishedAt;

	@Column(name = "next_attempt_at", nullable = false)
	private Instant nextAttemptAt;

	@Column(nullable = false)
	private int attempts;

}
