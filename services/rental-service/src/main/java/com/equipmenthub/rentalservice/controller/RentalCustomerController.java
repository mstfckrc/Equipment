package com.equipmenthub.rentalservice.controller;

import com.equipmenthub.rentalservice.dto.request.ReservationCheckoutRequest;
import com.equipmenthub.rentalservice.dto.request.ReservationItemStatusRequest;
import com.equipmenthub.rentalservice.dto.response.AvailabilityCalendarResponse;
import com.equipmenthub.rentalservice.dto.response.ReservationCheckoutResponse;
import com.equipmenthub.rentalservice.dto.response.ReservationItemResponse;
import com.equipmenthub.rentalservice.dto.response.ReservationResponse;
import com.equipmenthub.rentalservice.entity.ReservationItemStatus;
import com.equipmenthub.rentalservice.exception.RentalException;
import com.equipmenthub.rentalservice.service.RentalReservationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rentals")
public class RentalCustomerController {

	@Autowired
	private RentalReservationService rentalReservationService;

	@GetMapping("/stores/{storeId}/listings/{listingId}/availability")
	public AvailabilityCalendarResponse availability(
			@PathVariable UUID storeId,
			@PathVariable UUID listingId,
			@RequestParam LocalDate fromDate,
			@RequestParam LocalDate toDateExclusive,
			@AuthenticationPrincipal Jwt jwt) {
		return rentalReservationService.getAvailability(
				storeId, listingId, fromDate, toDateExclusive, jwt.getTokenValue());
	}

	@PostMapping("/reservations")
	public ResponseEntity<ReservationCheckoutResponse> checkout(
			@RequestHeader("Idempotency-Key") String idempotencyKey,
			@Valid @RequestBody ReservationCheckoutRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		ReservationCheckoutResponse response = rentalReservationService.checkout(
				request, idempotencyKey, jwt.getTokenValue(), jwt.getSubject());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/reservations/me")
	public List<ReservationResponse> reservations(@AuthenticationPrincipal Jwt jwt) {
		return rentalReservationService.getCustomerReservations(jwt.getSubject());
	}

	@PatchMapping("/reservations/me/items/{itemId}")
	public ReservationItemResponse updateReservationItemStatus(
			@PathVariable UUID itemId,
			@Valid @RequestBody ReservationItemStatusRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return switch (request.getStatus()) {
			case CANCELLED -> rentalReservationService.cancelCustomerItem(itemId, jwt.getSubject());
			case RETURN_REQUESTED -> rentalReservationService.requestCustomerReturn(itemId, jwt.getSubject());
			default -> throw new RentalException(HttpStatus.BAD_REQUEST,
					"Customers may cancel before pickup or request geri teslim after pickup");
		};
	}

}
