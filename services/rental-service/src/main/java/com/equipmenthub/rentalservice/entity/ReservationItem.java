package com.equipmenthub.rentalservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "reservation_item", indexes = {
		@Index(name = "idx_reservation_item_listing_dates", columnList = "listing_id,status,pickup_date,return_date"),
		@Index(name = "idx_reservation_item_reservation", columnList = "reservation_id,pickup_date")
})
@Getter
@Setter
@NoArgsConstructor
public class ReservationItem {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "reservation_id", nullable = false)
	private Reservation reservation;

	@Column(name = "listing_id", nullable = false)
	private UUID listingId;

	@Column(name = "listing_name", nullable = false, length = 200)
	private String listingName;

	@Column(nullable = false)
	private int quantity;

	@Column(name = "pickup_date", nullable = false)
	private LocalDate pickupDate;

	@Column(name = "return_date", nullable = false)
	private LocalDate returnDate;

	@Column(name = "daily_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal dailyPrice;

	@Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal totalAmount;

	@Column(name = "currency_code", nullable = false, length = 3)
	private String currencyCode = "TRY";

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ReservationItemStatus status = ReservationItemStatus.CONFIRMED;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

}
