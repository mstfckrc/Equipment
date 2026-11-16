package com.equipmenthub.rentalservice.controller;

import com.equipmenthub.rentalservice.dto.response.ConfirmationResponse;
import com.equipmenthub.rentalservice.service.ConfirmationPersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/rentals/checkouts")
public class RentalConfirmationController {

	@Autowired
	private ConfirmationPersistenceService persistenceService;

	@GetMapping("/{checkoutId}/confirmation")
	public ConfirmationResponse getConfirmation(@PathVariable UUID checkoutId, @AuthenticationPrincipal Jwt jwt) {
		return persistenceService.getOwnConfirmation(checkoutId, UUID.fromString(jwt.getSubject()));
	}

}
