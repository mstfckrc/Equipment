package com.equipmenthub.rentalservice.controller;

import com.equipmenthub.rentalservice.dto.request.ReservationItemStatusRequest;
import com.equipmenthub.rentalservice.dto.response.ReservationItemResponse;
import com.equipmenthub.rentalservice.dto.response.ReservationResponse;
import com.equipmenthub.rentalservice.entity.ReservationItemStatus;
import com.equipmenthub.rentalservice.exception.RentalException;
import com.equipmenthub.rentalservice.service.RentalReservationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rentals/stores/me/reservations")
public class RentalStoreReservationController {

	@Autowired
	private RentalReservationService rentalReservationService;

	@GetMapping
	public List<ReservationResponse> reservations(@AuthenticationPrincipal Jwt jwt) {
		return rentalReservationService.getStoreReservations(jwt.getTokenValue());
	}

	@PatchMapping("/items/{itemId}")
	public ReservationItemResponse updateReservationItemStatus(
			@PathVariable UUID itemId,
			@Valid @RequestBody ReservationItemStatusRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return switch (request.getStatus()) {
			case CANCELLED -> rentalReservationService.cancelStoreItem(itemId, jwt.getTokenValue());
			case PICKED_UP -> rentalReservationService.handoverStoreItem(itemId, request, jwt.getTokenValue());
			case RETURN_RECEIVED -> rentalReservationService.receiveStoreReturn(itemId, jwt.getTokenValue());
			case RETURNED -> rentalReservationService.inspectStoreReturn(itemId, request, jwt.getTokenValue());
			default -> throw new RentalException(HttpStatus.BAD_REQUEST,
					"Stores may cancel before pickup, record pickup, receive geri teslim, or finish inspection");
		};
	}

}
