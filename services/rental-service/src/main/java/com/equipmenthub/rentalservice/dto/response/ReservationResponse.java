package com.equipmenthub.rentalservice.dto.response;

import com.equipmenthub.rentalservice.entity.Reservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

	private UUID reservationId;
	private UUID checkoutId;
	private UUID storeId;
	private BigDecimal totalAmount;
	private String currencyCode;
	private Instant createdAt;
	private List<ReservationItemResponse> items;

	public static ReservationResponse from(Reservation reservation, List<ReservationItemResponse> items) {
		return ReservationResponse.builder()
				.reservationId(reservation.getId())
				.checkoutId(reservation.getCheckout().getId())
				.storeId(reservation.getStoreId())
				.totalAmount(reservation.getTotalAmount())
				.currencyCode(reservation.getCheckout().getCurrencyCode())
				.createdAt(reservation.getCreatedAt())
				.items(items)
				.build();
	}

}
