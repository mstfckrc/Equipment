package com.equipmenthub.rentalservice.service;

import com.equipmenthub.rentalservice.dto.request.EquipmentUnitStatusRequest;
import com.equipmenthub.rentalservice.dto.response.EquipmentUnitResponse;
import com.equipmenthub.rentalservice.dto.response.RentalInventoryResponse;
import com.equipmenthub.rentalservice.dto.response.RentalInventorySummaryResponse;
import com.equipmenthub.rentalservice.entity.EquipmentUnit;
import com.equipmenthub.rentalservice.entity.EquipmentUnitStatus;
import com.equipmenthub.rentalservice.entity.RentalInventory;
import com.equipmenthub.rentalservice.entity.ReservationItem;
import com.equipmenthub.rentalservice.entity.ReservationItemStatus;
import com.equipmenthub.rentalservice.exception.RentalException;
import com.equipmenthub.rentalservice.repository.EquipmentUnitCount;
import com.equipmenthub.rentalservice.repository.EquipmentUnitRepository;
import com.equipmenthub.rentalservice.repository.RentalInventoryRepository;
import com.equipmenthub.rentalservice.repository.ReservationItemRepository;
import com.equipmenthub.rentalservice.repository.ReservationInterval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.TreeMap;

@Service
public class RentalInventoryPersistenceService {

	private static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Istanbul");

	@Autowired
	private RentalInventoryRepository rentalInventoryRepository;

	@Autowired
	private EquipmentUnitRepository equipmentUnitRepository;

	@Autowired
	private ReservationItemRepository reservationItemRepository;

	@Transactional(readOnly = true)
	public List<RentalInventorySummaryResponse> getStoreInventory(UUID storeId) {
		List<RentalInventory> inventories = rentalInventoryRepository.findAllByStoreIdOrderByCreatedAtDesc(storeId);
		Map<UUID, Map<EquipmentUnitStatus, Long>> counts = getUnitCounts(inventories);
		return inventories.stream().map(inventory -> summary(inventory, counts.get(inventory.getId()))).toList();
	}

	@Transactional(readOnly = true)
	public RentalInventoryResponse getInventory(UUID storeId, UUID listingId) {
		RentalInventory inventory = rentalInventoryRepository.findByStoreIdAndListingId(storeId, listingId)
				.orElseThrow(() -> new RentalException(HttpStatus.NOT_FOUND, "Inventory not found"));
		return detail(inventory);
	}

	@Transactional
	public RentalInventoryResponse setInventory(UUID storeId, UUID listingId, int targetUnits) {
		RentalInventory inventory = rentalInventoryRepository.findForUpdate(storeId, listingId).orElse(null);
		if (inventory == null) {
			inventory = new RentalInventory();
			inventory.setStoreId(storeId);
			inventory.setListingId(listingId);
			inventory = rentalInventoryRepository.saveAndFlush(inventory);
		}

		long currentUnits = equipmentUnitRepository.countByInventory_IdAndStatusNot(
				inventory.getId(), EquipmentUnitStatus.RETIRED);
		if (targetUnits > currentUnits) {
			addUnits(inventory, targetUnits - currentUnits);
		}
		else if (targetUnits < currentUnits) {
			long unitsToRetire = currentUnits - targetUnits;
			long currentReadyUnits = equipmentUnitRepository.countByInventory_IdAndStatus(
					inventory.getId(), EquipmentUnitStatus.READY);
			if (unitsToRetire > currentReadyUnits) {
				throw new RentalException(HttpStatus.CONFLICT,
						"Inventory cannot be reduced while units are rented or otherwise unavailable");
			}
			long targetReadyUnits = currentReadyUnits - unitsToRetire;
			if (targetReadyUnits < maximumCommittedUnits(listingId)) {
				throw new RentalException(HttpStatus.CONFLICT,
						"Inventory cannot be reduced below quantities reserved for upcoming dates");
			}
			retireReadyUnits(inventory, unitsToRetire);
		}
		return detail(rentalInventoryRepository.saveAndFlush(inventory));
	}

	@Transactional
	public EquipmentUnitResponse updateUnitStatus(
			UUID storeId, UUID unitId, EquipmentUnitStatus requestedStatus) {
		EquipmentUnit current = equipmentUnitRepository.findWithInventory(unitId)
				.filter(unit -> unit.getInventory().getStoreId().equals(storeId))
				.orElseThrow(() -> new RentalException(HttpStatus.NOT_FOUND, "Equipment unit not found"));
		RentalInventory inventory = rentalInventoryRepository.findForUpdate(
				storeId, current.getInventory().getListingId())
				.orElseThrow(() -> new RentalException(HttpStatus.NOT_FOUND, "Inventory not found"));
		EquipmentUnit unit = equipmentUnitRepository.findForUpdate(unitId, storeId)
				.orElseThrow(() -> new RentalException(HttpStatus.NOT_FOUND, "Equipment unit not found"));

		if (requestedStatus == EquipmentUnitStatus.RENTED
				|| requestedStatus == EquipmentUnitStatus.RETURN_PENDING_INSPECTION
				|| unit.getStatus() == EquipmentUnitStatus.RENTED
				|| unit.getStatus() == EquipmentUnitStatus.RETURN_PENDING_INSPECTION) {
			throw new RentalException(HttpStatus.CONFLICT,
					"Pickup and geri teslim equipment states are managed by reservation operations");
		}

		if (unit.getStatus() == EquipmentUnitStatus.RETIRED
				&& requestedStatus != EquipmentUnitStatus.RETIRED) {
			throw new RentalException(HttpStatus.CONFLICT, "A retired equipment unit cannot be reactivated");
		}
		if (unit.getStatus() == EquipmentUnitStatus.READY && requestedStatus != EquipmentUnitStatus.READY) {
			long readyUnits = equipmentUnitRepository.countByInventory_IdAndStatus(
					inventory.getId(), EquipmentUnitStatus.READY);
			if (readyUnits - 1 < maximumCommittedUnits(inventory.getListingId())) {
				throw new RentalException(HttpStatus.CONFLICT,
						"This unit is needed for an upcoming reservation and cannot be made unavailable");
			}
		}

		unit.setStatus(requestedStatus);
		return EquipmentUnitResponse.from(equipmentUnitRepository.saveAndFlush(unit));
	}

	private void addUnits(RentalInventory inventory, long count) {
		List<EquipmentUnit> units = new ArrayList<>();
		for (long index = 0; index < count; index++) {
			long unitNumber = inventory.getNextUnitNumber();
			EquipmentUnit unit = new EquipmentUnit();
			unit.setInventory(inventory);
			unit.setUnitNumber(unitNumber);
			unit.setUnitCode("#" + String.format("%03d", unitNumber));
			unit.setStatus(EquipmentUnitStatus.READY);
			units.add(unit);
			inventory.setNextUnitNumber(unitNumber + 1);
		}
		equipmentUnitRepository.saveAllAndFlush(units);
	}

	private void retireReadyUnits(RentalInventory inventory, long count) {
		List<EquipmentUnit> readyUnits = equipmentUnitRepository
				.findForRetirement(inventory.getId(), EquipmentUnitStatus.READY);
		if (readyUnits.size() < count) {
			throw new RentalException(HttpStatus.CONFLICT,
					"Inventory cannot be reduced while units are rented or otherwise unavailable");
		}
		List<EquipmentUnit> unitsToRetire = readyUnits.stream().limit(count).toList();
		unitsToRetire.forEach(unit -> unit.setStatus(EquipmentUnitStatus.RETIRED));
		equipmentUnitRepository.saveAllAndFlush(unitsToRetire);
	}

	private RentalInventoryResponse detail(RentalInventory inventory) {
		List<EquipmentUnit> units = equipmentUnitRepository
				.findAllByInventory_IdOrderByUnitNumberAsc(inventory.getId());
		Map<EquipmentUnitStatus, Long> counts = new EnumMap<>(EquipmentUnitStatus.class);
		for (EquipmentUnit unit : units) {
			counts.merge(unit.getStatus(), 1L, Long::sum);
		}
		return RentalInventoryResponse.builder()
				.listingId(inventory.getListingId())
				.totalUnits(activeUnits(counts))
				.readyUnits(counts.getOrDefault(EquipmentUnitStatus.READY, 0L))
				.maintenanceUnits(counts.getOrDefault(EquipmentUnitStatus.MAINTENANCE, 0L))
				.retiredUnits(counts.getOrDefault(EquipmentUnitStatus.RETIRED, 0L))
				.rentedUnits(counts.getOrDefault(EquipmentUnitStatus.RENTED, 0L))
				.pendingInspectionUnits(counts.getOrDefault(EquipmentUnitStatus.RETURN_PENDING_INSPECTION, 0L))
				.units(units.stream().map(EquipmentUnitResponse::from).toList())
				.build();
	}

	private Map<UUID, Map<EquipmentUnitStatus, Long>> getUnitCounts(List<RentalInventory> inventories) {
		if (inventories.isEmpty()) {
			return Map.of();
		}
		Collection<UUID> inventoryIds = inventories.stream().map(RentalInventory::getId).toList();
		Map<UUID, Map<EquipmentUnitStatus, Long>> counts = new HashMap<>();
		for (EquipmentUnitCount unitCount : equipmentUnitRepository.countByInventoryIds(inventoryIds)) {
			counts.computeIfAbsent(unitCount.getInventoryId(), ignored -> new EnumMap<>(EquipmentUnitStatus.class))
					.put(unitCount.getStatus(), unitCount.getUnitCount());
		}
		return counts;
	}

	private RentalInventorySummaryResponse summary(
			RentalInventory inventory, Map<EquipmentUnitStatus, Long> counts) {
		long readyUnits = count(counts, EquipmentUnitStatus.READY);
		long maintenanceUnits = count(counts, EquipmentUnitStatus.MAINTENANCE);
		return RentalInventorySummaryResponse.builder()
				.listingId(inventory.getListingId())
				.totalUnits(activeUnits(counts))
				.readyUnits(readyUnits)
				.maintenanceUnits(maintenanceUnits)
				.retiredUnits(count(counts, EquipmentUnitStatus.RETIRED))
				.rentedUnits(count(counts, EquipmentUnitStatus.RENTED))
				.pendingInspectionUnits(count(counts, EquipmentUnitStatus.RETURN_PENDING_INSPECTION))
				.build();
	}

	private long activeUnits(Map<EquipmentUnitStatus, Long> counts) {
		return count(counts, EquipmentUnitStatus.READY)
				+ count(counts, EquipmentUnitStatus.MAINTENANCE)
				+ count(counts, EquipmentUnitStatus.RENTED)
				+ count(counts, EquipmentUnitStatus.RETURN_PENDING_INSPECTION);
	}

	private long count(Map<EquipmentUnitStatus, Long> counts, EquipmentUnitStatus status) {
		return counts == null ? 0 : counts.getOrDefault(status, 0L);
	}

	private long maximumCommittedUnits(UUID listingId) {
		LocalDate today = LocalDate.now(BUSINESS_ZONE);
		TreeMap<LocalDate, Long> events = new TreeMap<>();
		for (ReservationInterval item : reservationItemRepository.findActiveAfter(
				listingId, ReservationItemStatus.CONFIRMED, today)) {
			LocalDate start = item.getPickupDate().isBefore(today) ? today : item.getPickupDate();
			events.merge(start, (long) item.getQuantity(), Long::sum);
			events.merge(item.getReturnDate(), -(long) item.getQuantity(), Long::sum);
		}
		long activeUnits = 0;
		long maximumUnits = 0;
		for (long change : events.values()) {
			activeUnits += change;
			maximumUnits = Math.max(maximumUnits, activeUnits);
		}
		return maximumUnits;
	}

}
