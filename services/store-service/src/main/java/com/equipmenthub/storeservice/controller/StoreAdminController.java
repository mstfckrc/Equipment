package com.equipmenthub.storeservice.controller;

import com.equipmenthub.storeservice.dto.request.StoreStatusUpdateRequest;
import com.equipmenthub.storeservice.dto.response.StoreProfileResponse;
import com.equipmenthub.storeservice.dto.response.StoreSummaryResponse;
import com.equipmenthub.storeservice.service.StoreProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stores/admin")
public class StoreAdminController {

	@Autowired
	private StoreProfileService storeProfileService;

	@GetMapping
	public List<StoreSummaryResponse> stores() {
		return storeProfileService.getStores();
	}

	@GetMapping("/{storeId}")
	public StoreProfileResponse store(@PathVariable UUID storeId) {
		return storeProfileService.getStore(storeId);
	}

	@PatchMapping("/{storeId}")
	public StoreProfileResponse updateStatus(
			@PathVariable UUID storeId, @Valid @RequestBody StoreStatusUpdateRequest request) {
		return storeProfileService.updateStoreStatus(storeId, request.getStatus());
	}

}
