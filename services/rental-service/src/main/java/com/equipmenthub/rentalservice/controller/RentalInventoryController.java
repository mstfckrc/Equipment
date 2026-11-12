package com.equipmenthub.rentalservice.controller;

import com.equipmenthub.rentalservice.dto.request.EquipmentUnitStatusRequest;
import com.equipmenthub.rentalservice.dto.request.RentalInventoryRequest;
import com.equipmenthub.rentalservice.dto.response.EquipmentUnitResponse;
import com.equipmenthub.rentalservice.dto.response.RentalInventoryResponse;
import com.equipmenthub.rentalservice.dto.response.RentalInventorySummaryResponse;
import com.equipmenthub.rentalservice.service.RentalInventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rentals/stores/me")
public class RentalInventoryController {

	@Autowired
	private RentalInventoryService rentalInventoryService;

	@GetMapping("/inventory")
	public List<RentalInventorySummaryResponse> inventory(@AuthenticationPrincipal Jwt jwt) {
		return rentalInventoryService.getOwnInventory(jwt.getTokenValue());
	}

	@GetMapping("/listings/{listingId}/inventory")
	public RentalInventoryResponse listingInventory(
			@PathVariable UUID listingId, @AuthenticationPrincipal Jwt jwt) {
		return rentalInventoryService.getListingInventory(listingId, jwt.getTokenValue());
	}

	@PutMapping("/listings/{listingId}/inventory")
	public RentalInventoryResponse setListingInventory(
			@PathVariable UUID listingId, @Valid @RequestBody RentalInventoryRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return rentalInventoryService.setListingInventory(
				listingId, request.getTotalUnits(), jwt.getTokenValue());
	}

	@PatchMapping("/inventory/units/{unitId}")
	public EquipmentUnitResponse updateUnitStatus(
			@PathVariable UUID unitId, @Valid @RequestBody EquipmentUnitStatusRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return rentalInventoryService.updateUnitStatus(unitId, request.getStatus(), jwt.getTokenValue());
	}

}
