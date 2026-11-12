package com.equipmenthub.rentalservice.service;

import com.equipmenthub.rentalservice.client.CatalogServiceClient;
import com.equipmenthub.rentalservice.client.StoreServiceClient;
import com.equipmenthub.rentalservice.dto.response.EquipmentUnitResponse;
import com.equipmenthub.rentalservice.dto.response.RentalInventoryResponse;
import com.equipmenthub.rentalservice.dto.response.RentalInventorySummaryResponse;
import com.equipmenthub.rentalservice.entity.EquipmentUnitStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RentalInventoryService {

	@Autowired
	private StoreServiceClient storeServiceClient;

	@Autowired
	private CatalogServiceClient catalogServiceClient;

	@Autowired
	private RentalInventoryPersistenceService rentalInventoryPersistenceService;

	public List<RentalInventorySummaryResponse> getOwnInventory(String accessToken) {
		UUID storeId = storeServiceClient.getActiveStoreId(accessToken);
		return rentalInventoryPersistenceService.getStoreInventory(storeId);
	}

	public RentalInventoryResponse getListingInventory(UUID listingId, String accessToken) {
		UUID storeId = storeServiceClient.getActiveStoreId(accessToken);
		catalogServiceClient.requireListingOwnedBy(listingId, storeId, accessToken);
		return rentalInventoryPersistenceService.getInventory(storeId, listingId);
	}

	public RentalInventoryResponse setListingInventory(UUID listingId, int totalUnits, String accessToken) {
		UUID storeId = storeServiceClient.getActiveStoreId(accessToken);
		catalogServiceClient.requireListingOwnedBy(listingId, storeId, accessToken);
		return rentalInventoryPersistenceService.setInventory(storeId, listingId, totalUnits);
	}

	public EquipmentUnitResponse updateUnitStatus(
			UUID unitId, EquipmentUnitStatus status, String accessToken) {
		UUID storeId = storeServiceClient.getActiveStoreId(accessToken);
		return rentalInventoryPersistenceService.updateUnitStatus(storeId, unitId, status);
	}

}
