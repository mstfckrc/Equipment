package com.equipmenthub.rentalservice.dto.response;

import com.equipmenthub.rentalservice.entity.RentalCheckout;
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
public class ReservationCheckoutResponse {

	private UUID checkoutId;
	private BigDecimal totalAmount;
	private String currencyCode;
	private Instant createdAt;
	private List<ReservationResponse> reservations;

	public static ReservationCheckoutResponse from(
			RentalCheckout checkout, List<ReservationResponse> reservations) {
		return ReservationCheckoutResponse.builder()
				.checkoutId(checkout.getId())
				.totalAmount(checkout.getTotalAmount())
				.currencyCode(checkout.getCurrencyCode())
				.createdAt(checkout.getCreatedAt())
				.reservations(reservations)
				.build();
	}

}
