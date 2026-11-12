package com.equipmenthub.rentalservice.service;

import com.equipmenthub.rentalservice.dto.request.ReservationItemRequest;
import com.equipmenthub.rentalservice.dto.request.UnitReturnInspectionRequest;
import com.equipmenthub.rentalservice.dto.response.AvailabilityCalendarResponse;
import com.equipmenthub.rentalservice.dto.response.AvailabilityConflictResponse;
import com.equipmenthub.rentalservice.dto.response.AvailabilityDayResponse;
import com.equipmenthub.rentalservice.dto.response.CatalogListingResponse;
import com.equipmenthub.rentalservice.dto.response.ReservationCheckoutResponse;
import com.equipmenthub.rentalservice.dto.response.ReservationItemResponse;
import com.equipmenthub.rentalservice.dto.response.ReservationResponse;
import com.equipmenthub.rentalservice.dto.response.ReservationUnitAssignmentResponse;
import com.equipmenthub.rentalservice.entity.EquipmentUnit;
import com.equipmenthub.rentalservice.entity.EquipmentUnitStatus;
import com.equipmenthub.rentalservice.entity.RentalCheckout;
import com.equipmenthub.rentalservice.entity.RentalInventory;
import com.equipmenthub.rentalservice.entity.Reservation;
import com.equipmenthub.rentalservice.entity.ReservationItem;
import com.equipmenthub.rentalservice.entity.ReservationItemStatus;
import com.equipmenthub.rentalservice.entity.ReservationUnitAssignment;
import com.equipmenthub.rentalservice.exception.RentalException;
import com.equipmenthub.rentalservice.repository.EquipmentUnitRepository;
import com.equipmenthub.rentalservice.repository.RentalCheckoutRepository;
import com.equipmenthub.rentalservice.repository.RentalInventoryRepository;
import com.equipmenthub.rentalservice.repository.ReservationItemRepository;
import com.equipmenthub.rentalservice.repository.ReservationInterval;
import com.equipmenthub.rentalservice.repository.ReservationRepository;
import com.equipmenthub.rentalservice.repository.ReservationUnitAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RentalReservationPersistenceService {

	private static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Istanbul");

	@Autowired
	private RentalCheckoutRepository rentalCheckoutRepository;

	@Autowired
	private ReservationRepository reservationRepository;

	@Autowired
	private ReservationItemRepository reservationItemRepository;

	@Autowired
	private RentalInventoryRepository rentalInventoryRepository;

	@Autowired
	private EquipmentUnitRepository equipmentUnitRepository;

	@Autowired
	private ReservationUnitAssignmentRepository reservationUnitAssignmentRepository;

	@Transactional(readOnly = true)
	public AvailabilityCalendarResponse getAvailability(
			UUID storeId, UUID listingId, String listingName, BigDecimal dailyPrice,
			LocalDate fromDate, LocalDate toDateExclusive) {
		Optional<RentalInventory> inventory = rentalInventoryRepository.findByStoreIdAndListingId(storeId, listingId);
		int readyUnits = inventory.map(value -> Math.toIntExact(equipmentUnitRepository
				.countByInventory_IdAndStatus(value.getId(), EquipmentUnitStatus.READY))).orElse(0);
		List<ReservationInterval> overlaps = reservationItemRepository.findOverlapping(
				listingId, ReservationItemStatus.CONFIRMED, fromDate, toDateExclusive);
		TreeMap<LocalDate, Long> reservedEvents = new TreeMap<>();
		for (ReservationInterval item : overlaps) {
			addEvent(reservedEvents, item.getPickupDate().isAfter(fromDate) ? item.getPickupDate() : fromDate,
					item.getQuantity());
			addEvent(reservedEvents,
					item.getReturnDate().isBefore(toDateExclusive) ? item.getReturnDate() : toDateExclusive,
					-item.getQuantity());
		}

		List<AvailabilityDayResponse> days = new ArrayList<>();
		long reserved = 0;
		for (LocalDate date = fromDate; date.isBefore(toDateExclusive); date = date.plusDays(1)) {
			reserved += reservedEvents.getOrDefault(date, 0L);
			int available = date.isAfter(LocalDate.now(BUSINESS_ZONE))
					? Math.toIntExact(Math.max(0L, readyUnits - reserved)) : 0;
			days.add(AvailabilityDayResponse.builder()
					.date(date)
					.readyUnits(readyUnits)
					.reservedUnits(Math.toIntExact(reserved))
					.remainingUnits(available)
					.build());
		}
		return AvailabilityCalendarResponse.builder()
				.storeId(storeId)
				.listingId(listingId)
				.fromDate(fromDate)
				.toDateExclusive(toDateExclusive)
				.currencyCode("TRY")
				.listingName(listingName)
				.dailyPrice(dailyPrice)
				.days(days)
				.build();
	}

	@Transactional(readOnly = true)
	public ReservationCheckoutResponse findCheckout(UUID customerId, String idempotencyKey, String fingerprint) {
		return rentalCheckoutRepository.findByCustomerIdAndIdempotencyKey(customerId, idempotencyKey)
				.map(checkout -> existingCheckout(checkout, fingerprint))
				.orElse(null);
	}

	@Transactional
	public ReservationCheckoutResponse createCheckout(
			UUID customerId, String idempotencyKey, String fingerprint, List<PreparedLine> lines) {
		RentalCheckout existing = rentalCheckoutRepository
				.findByCustomerIdAndIdempotencyKey(customerId, idempotencyKey).orElse(null);
		if (existing != null) {
			return existingCheckout(existing, fingerprint);
		}

		Map<ListingKey, RentalInventory> lockedInventories = lockInventories(lines);
		RentalCheckout checkoutAfterLock = rentalCheckoutRepository
				.findByCustomerIdAndIdempotencyKey(customerId, idempotencyKey).orElse(null);
		if (checkoutAfterLock != null) {
			return existingCheckout(checkoutAfterLock, fingerprint);
		}
		checkAvailability(lines, lockedInventories);

		BigDecimal total = lines.stream().map(this::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		RentalCheckout checkout = new RentalCheckout();
		checkout.setCustomerId(customerId);
		checkout.setIdempotencyKey(idempotencyKey);
		checkout.setRequestFingerprint(fingerprint);
		checkout.setTotalAmount(total);
		checkout.setCurrencyCode("TRY");
		checkout = rentalCheckoutRepository.saveAndFlush(checkout);

		Map<UUID, List<PreparedLine>> byStore = lines.stream().collect(Collectors.groupingBy(
				line -> line.request().getStoreId(), LinkedHashMap::new, Collectors.toList()));
		for (Map.Entry<UUID, List<PreparedLine>> entry : byStore.entrySet()) {
			Reservation reservation = new Reservation();
			reservation.setCheckout(checkout);
			reservation.setStoreId(entry.getKey());
			reservation.setTotalAmount(entry.getValue().stream()
					.map(this::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
			reservation = reservationRepository.saveAndFlush(reservation);
			List<ReservationItem> items = new ArrayList<>();
			for (PreparedLine line : entry.getValue()) {
				ReservationItemRequest request = line.request();
				ReservationItem item = new ReservationItem();
				item.setReservation(reservation);
				item.setListingId(request.getListingId());
				item.setListingName(line.listing().getName());
				item.setQuantity(request.getQuantity());
				item.setPickupDate(request.getPickupDate());
				item.setReturnDate(request.getReturnDate());
				item.setDailyPrice(line.listing().getDailyPrice());
				item.setTotalAmount(lineTotal(line));
				item.setCurrencyCode("TRY");
				item.setStatus(ReservationItemStatus.CONFIRMED);
				items.add(item);
			}
			reservationItemRepository.saveAllAndFlush(items);
		}
		return checkoutResponse(checkout);
	}

	@Transactional(readOnly = true)
	public List<ReservationResponse> getCustomerReservations(UUID customerId) {
		return reservationResponses(reservationRepository.findAllWithCheckoutByCustomerId(customerId));
	}

	@Transactional(readOnly = true)
	public List<ReservationResponse> getStoreReservations(UUID storeId) {
		return reservationResponses(reservationRepository.findAllWithCheckoutByStoreId(storeId));
	}

	@Transactional
	public ReservationItemResponse requestCustomerReturn(UUID itemId, UUID customerId, LocalDateTime now) {
		ReservationItem target = reservationItemRepository.findWithReservation(itemId)
				.orElseThrow(() -> notFound("Reservation item not found"));
		lockInventoryForItem(target);
		ReservationItem item = reservationItemRepository.findForCustomerUpdate(itemId, customerId)
				.orElseThrow(() -> notFound("Reservation item not found"));
		if (item.getStatus() == ReservationItemStatus.RETURN_REQUESTED) {
			return reservationItemResponse(item, now);
		}
		if (item.getStatus() != ReservationItemStatus.PICKED_UP) {
			throw conflict("INVALID_RESERVATION_TRANSITION",
					"Return can be requested only after the equipment has been picked up");
		}
		item.setStatus(ReservationItemStatus.RETURN_REQUESTED);
		return reservationItemResponse(reservationItemRepository.saveAndFlush(item), now);
	}

	@Transactional
	public ReservationItemResponse handoverStoreItem(
			UUID itemId, UUID storeId, List<UUID> requestedUnitIds, LocalDateTime now) {
		ReservationItem target = reservationItemRepository.findWithReservation(itemId)
				.filter(item -> item.getReservation().getStoreId().equals(storeId))
				.orElseThrow(() -> notFound("Reservation item not found"));
		RentalInventory inventory = lockInventoryForItem(target);
		ReservationItem item = reservationItemRepository.findForStoreUpdate(itemId, storeId)
				.orElseThrow(() -> notFound("Reservation item not found"));
		if (item.getStatus() == ReservationItemStatus.PICKED_UP
				|| item.getStatus() == ReservationItemStatus.RETURN_REQUESTED) {
			return reservationItemResponse(item, now);
		}
		if (item.getStatus() != ReservationItemStatus.CONFIRMED) {
			throw conflict("INVALID_RESERVATION_TRANSITION",
					"Only a confirmed reservation can be handed over");
		}
		if (now.isBefore(item.getPickupDate().atTime(LocalTime.NOON))) {
			throw conflict("PICKUP_NOT_STARTED", "Equipment cannot be handed over before the scheduled pickup time");
		}
		if (!now.isBefore(item.getReturnDate().atTime(LocalTime.of(9, 0)))) {
			throw conflict("PICKUP_WINDOW_CLOSED", "The scheduled rental period has ended and pickup can no longer be recorded");
		}
		if (requestedUnitIds != null && (requestedUnitIds.size() != item.getQuantity()
				|| requestedUnitIds.stream().distinct().count() != item.getQuantity())) {
			throw invalidRequest("Select exactly one distinct equipment unit for each rented item");
		}

		List<EquipmentUnit> units = requestedUnitIds == null
				? equipmentUnitRepository.findReadyForHandover(inventory.getId(), EquipmentUnitStatus.READY)
						.stream().limit(item.getQuantity()).toList()
				: equipmentUnitRepository.findSelectedReadyForHandover(
					inventory.getId(), requestedUnitIds, EquipmentUnitStatus.READY);
		if (units.size() != item.getQuantity()) {
			throw conflict("EQUIPMENT_UNAVAILABLE",
					"The requested reservation cannot be assigned its equipment units");
		}

		Instant handedOverAt = now.atZone(BUSINESS_ZONE).toInstant();
		List<ReservationUnitAssignment> assignments = new ArrayList<>();
		for (EquipmentUnit unit : units) {
			unit.setStatus(EquipmentUnitStatus.RENTED);
			ReservationUnitAssignment assignment = new ReservationUnitAssignment();
			assignment.setReservationItem(item);
			assignment.setEquipmentUnit(unit);
			assignment.setHandedOverAt(handedOverAt);
			assignments.add(assignment);
		}
		reservationUnitAssignmentRepository.saveAllAndFlush(assignments);
		equipmentUnitRepository.saveAll(units);
		item.setStatus(ReservationItemStatus.PICKED_UP);
		return reservationItemResponse(reservationItemRepository.saveAndFlush(item), now);
	}

	@Transactional
	public ReservationItemResponse receiveStoreReturn(UUID itemId, UUID storeId, LocalDateTime now) {
		ReservationItem item = lockStoreItem(itemId, storeId);
		if (item.getStatus() == ReservationItemStatus.RETURN_RECEIVED
				|| item.getStatus() == ReservationItemStatus.RETURNED) {
			return reservationItemResponse(item, now);
		}
		if (item.getStatus() != ReservationItemStatus.PICKED_UP
				&& item.getStatus() != ReservationItemStatus.RETURN_REQUESTED) {
			throw conflict("INVALID_RESERVATION_TRANSITION",
					"Only equipment that has been picked up can be received back");
		}
		List<ReservationUnitAssignment> assignments =
				reservationUnitAssignmentRepository.findForUpdateByReservationItemId(itemId);
		if (assignments.size() != item.getQuantity()) {
			throw conflict("EQUIPMENT_ASSIGNMENT_INCOMPLETE",
					"The reservation does not have a complete equipment assignment");
		}
		Instant receivedAt = now.atZone(BUSINESS_ZONE).toInstant();
		for (ReservationUnitAssignment assignment : assignments) {
			if (assignment.getEquipmentUnit().getStatus() != EquipmentUnitStatus.RENTED) {
				throw conflict("EQUIPMENT_STATE_CHANGED", "An assigned equipment unit is not currently rented");
			}
			assignment.setReceivedAt(receivedAt);
			assignment.getEquipmentUnit().setStatus(EquipmentUnitStatus.RETURN_PENDING_INSPECTION);
		}
		equipmentUnitRepository.saveAll(assignments.stream()
				.map(ReservationUnitAssignment::getEquipmentUnit).toList());
		item.setStatus(ReservationItemStatus.RETURN_RECEIVED);
		return reservationItemResponse(reservationItemRepository.saveAndFlush(item), now);
	}

	@Transactional
	public ReservationItemResponse inspectStoreReturn(
			UUID itemId, UUID storeId, List<UnitReturnInspectionRequest> inspections, LocalDateTime now) {
		ReservationItem item = lockStoreItem(itemId, storeId);
		if (item.getStatus() == ReservationItemStatus.RETURNED) {
			return reservationItemResponse(item, now);
		}
		if (item.getStatus() != ReservationItemStatus.RETURN_RECEIVED) {
			throw conflict("INVALID_RESERVATION_TRANSITION",
					"Equipment must be received before its return inspection is completed");
		}
		List<ReservationUnitAssignment> assignments =
				reservationUnitAssignmentRepository.findForUpdateByReservationItemId(itemId);
		if (assignments.size() != item.getQuantity() || inspections == null
				|| inspections.size() != assignments.size()) {
			throw invalidRequest("Submit an inspection result for every assigned equipment unit");
		}
		Map<UUID, UnitReturnInspectionRequest> inspectionByUnit = new LinkedHashMap<>();
		for (UnitReturnInspectionRequest inspection : inspections) {
			if (inspection == null || inspection.getUnitId() == null || inspection.getCondition() == null
					|| inspectionByUnit.putIfAbsent(inspection.getUnitId(), inspection) != null) {
				throw invalidRequest("Each assigned equipment unit must have one valid inspection result");
			}
			if (inspection.getCondition() != EquipmentUnitStatus.READY
					&& inspection.getCondition() != EquipmentUnitStatus.MAINTENANCE
					&& inspection.getCondition() != EquipmentUnitStatus.RETIRED) {
				throw invalidRequest("Return condition must be READY, MAINTENANCE, or RETIRED");
			}
		}

		Instant inspectedAt = now.atZone(BUSINESS_ZONE).toInstant();
		for (ReservationUnitAssignment assignment : assignments) {
			UUID unitId = assignment.getEquipmentUnit().getId();
			UnitReturnInspectionRequest inspection = inspectionByUnit.remove(unitId);
			if (inspection == null || assignment.getReceivedAt() == null
					|| assignment.getEquipmentUnit().getStatus() != EquipmentUnitStatus.RETURN_PENDING_INSPECTION) {
				throw invalidRequest("Inspection results must match all equipment assigned to this reservation");
			}
			EquipmentUnitStatus condition = inspection.getCondition();
			assignment.setInspectedAt(inspectedAt);
			assignment.setReturnCondition(condition);
			assignment.setDamageNote(normalizeOptional(inspection.getDamageNote()));
			assignment.setManualChargeAmount(inspection.getManualChargeAmount());
			assignment.getEquipmentUnit().setStatus(condition);
		}
		if (!inspectionByUnit.isEmpty()) {
			throw invalidRequest("Inspection results must match all equipment assigned to this reservation");
		}
		equipmentUnitRepository.saveAll(assignments.stream()
				.map(ReservationUnitAssignment::getEquipmentUnit).toList());
		reservationUnitAssignmentRepository.saveAll(assignments);
		item.setStatus(ReservationItemStatus.RETURNED);
		return reservationItemResponse(reservationItemRepository.saveAndFlush(item), now);
	}

	@Transactional
	public ReservationItemResponse cancelCustomerItem(UUID itemId, UUID customerId, LocalDateTime now) {
		ReservationItem target = reservationItemRepository.findWithReservation(itemId)
				.orElseThrow(() -> notFound("Reservation item not found"));
		lockInventoryForItem(target);
		ReservationItem item = reservationItemRepository.findForCustomerUpdate(itemId, customerId)
				.orElseThrow(() -> notFound("Reservation item not found"));
		if (item.getStatus() == ReservationItemStatus.CANCELLED) {
			return reservationItemResponse(item, now);
		}
		if (item.getStatus() != ReservationItemStatus.CONFIRMED) {
			throw conflict("INVALID_RESERVATION_TRANSITION", "A reservation can be cancelled only before pickup");
		}
		LocalDateTime pickup = item.getPickupDate().atTime(LocalTime.NOON);
		if (now.isAfter(pickup.minusHours(24))) {
			throw new RentalException(HttpStatus.CONFLICT, "CANCELLATION_WINDOW_CLOSED",
					"Contact the store to request cancellation within 24 hours of pickup", List.of(), List.of());
		}
		item.setStatus(ReservationItemStatus.CANCELLED);
		return reservationItemResponse(reservationItemRepository.saveAndFlush(item), now);
	}

	@Transactional
	public ReservationItemResponse cancelStoreItem(UUID itemId, UUID storeId, LocalDateTime now) {
		ReservationItem target = reservationItemRepository.findWithReservation(itemId)
				.filter(item -> item.getReservation().getStoreId().equals(storeId))
				.orElseThrow(() -> notFound("Reservation item not found"));
		lockInventoryForItem(target);
		ReservationItem item = reservationItemRepository.findForStoreUpdate(itemId, storeId)
				.orElseThrow(() -> notFound("Reservation item not found"));
		if (item.getStatus() == ReservationItemStatus.CANCELLED) {
			return reservationItemResponse(item, now);
		}
		if (item.getStatus() != ReservationItemStatus.CONFIRMED) {
			throw conflict("INVALID_RESERVATION_TRANSITION", "A reservation can be cancelled only before pickup");
		}
		if (!now.isBefore(item.getPickupDate().atTime(LocalTime.NOON))) {
			throw new RentalException(HttpStatus.CONFLICT, "RESERVATION_ALREADY_STARTED",
					"A reservation cannot be cancelled after its pickup time", List.of(), List.of());
		}
		item.setStatus(ReservationItemStatus.CANCELLED);
		return reservationItemResponse(reservationItemRepository.saveAndFlush(item), now);
	}

	private ReservationCheckoutResponse existingCheckout(RentalCheckout checkout, String fingerprint) {
		if (!checkout.getRequestFingerprint().equals(fingerprint)) {
			throw new RentalException(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUSED",
					"This Idempotency-Key was already used with a different request", List.of(), List.of());
		}
		return checkoutResponse(checkout);
	}

	private ReservationCheckoutResponse checkoutResponse(RentalCheckout checkout) {
		List<Reservation> reservations = reservationRepository.findAllWithCheckoutByCheckoutId(checkout.getId());
		return ReservationCheckoutResponse.from(checkout, reservationResponses(reservations));
	}

	private List<ReservationResponse> reservationResponses(List<Reservation> reservations) {
		if (reservations.isEmpty()) {
			return List.of();
		}
		List<UUID> reservationIds = reservations.stream().map(Reservation::getId).toList();
		List<ReservationItem> reservationItems = reservationItemRepository
				.findAllByReservation_IdInOrderByPickupDateAscIdAsc(reservationIds);
		Map<UUID, List<ReservationUnitAssignmentResponse>> unitsByItem = assignmentResponses(reservationItems);
		LocalDateTime now = LocalDateTime.now(BUSINESS_ZONE);
		Map<UUID, List<ReservationItemResponse>> itemsByReservation = reservationItems.stream()
				.collect(Collectors.groupingBy(item -> item.getReservation().getId(), LinkedHashMap::new,
						Collectors.mapping(item -> ReservationItemResponse.from(item,
								unitsByItem.getOrDefault(item.getId(), List.of()), now), Collectors.toList())));
		return reservations.stream().map(reservation -> ReservationResponse.from(reservation,
				itemsByReservation.getOrDefault(reservation.getId(), List.of()))).toList();
	}

	private ReservationItemResponse reservationItemResponse(ReservationItem item, LocalDateTime now) {
		Map<UUID, List<ReservationUnitAssignmentResponse>> byItem = assignmentResponses(List.of(item));
		return ReservationItemResponse.from(item, byItem.getOrDefault(item.getId(), List.of()), now);
	}

	private Map<UUID, List<ReservationUnitAssignmentResponse>> assignmentResponses(List<ReservationItem> items) {
		if (items.isEmpty()) {
			return Map.of();
		}
		List<UUID> itemIds = items.stream().map(ReservationItem::getId).toList();
		return reservationUnitAssignmentRepository.findAllByReservationItemIds(itemIds).stream()
				.collect(Collectors.groupingBy(assignment -> assignment.getReservationItem().getId(),
						LinkedHashMap::new,
						Collectors.mapping(ReservationUnitAssignmentResponse::from, Collectors.toList())));
	}

	private Map<ListingKey, RentalInventory> lockInventories(List<PreparedLine> lines) {
		List<ListingKey> keys = lines.stream()
				.map(line -> new ListingKey(line.request().getStoreId(), line.request().getListingId()))
				.distinct()
				.sorted(Comparator.comparing((ListingKey key) -> key.storeId().toString())
						.thenComparing(key -> key.listingId().toString()))
				.toList();
		Map<ListingKey, RentalInventory> inventories = new LinkedHashMap<>();
		for (ListingKey key : keys) {
			rentalInventoryRepository.findForUpdate(key.storeId(), key.listingId())
					.ifPresent(inventory -> inventories.put(key, inventory));
		}
		return inventories;
	}

	private void checkAvailability(List<PreparedLine> lines, Map<ListingKey, RentalInventory> inventories) {
		Map<ListingKey, List<PreparedLine>> byListing = lines.stream().collect(Collectors.groupingBy(
				line -> new ListingKey(line.request().getStoreId(), line.request().getListingId()), LinkedHashMap::new,
				Collectors.toList()));
		List<AvailabilityConflictResponse> conflicts = new ArrayList<>();
		for (Map.Entry<ListingKey, List<PreparedLine>> entry : byListing.entrySet()) {
			RentalInventory inventory = inventories.get(entry.getKey());
			if (inventory == null) {
				for (PreparedLine line : entry.getValue()) {
					conflicts.add(conflict(line, line.request().getPickupDate(), 0));
				}
				continue;
			}
			int readyUnits = Math.toIntExact(equipmentUnitRepository
					.countByInventory_IdAndStatus(inventory.getId(), EquipmentUnitStatus.READY));
			checkListingCapacity(entry.getKey().listingId(), entry.getValue(), readyUnits, conflicts);
		}
		if (!conflicts.isEmpty()) {
			throw new RentalException(HttpStatus.CONFLICT, "AVAILABILITY_CHANGED",
					"Some requested dates no longer have enough available units", List.of(), conflicts);
		}
	}

	private void checkListingCapacity(UUID listingId, List<PreparedLine> proposed, int readyUnits,
			List<AvailabilityConflictResponse> conflicts) {
		LocalDate fromDate = proposed.stream().map(line -> line.request().getPickupDate()).min(LocalDate::compareTo).orElseThrow();
		LocalDate toDate = proposed.stream().map(line -> line.request().getReturnDate()).max(LocalDate::compareTo).orElseThrow();
		TreeMap<LocalDate, CapacityEvent> events = new TreeMap<>();
		for (ReservationInterval item : reservationItemRepository.findOverlapping(
				listingId, ReservationItemStatus.CONFIRMED, fromDate, toDate)) {
			addCapacityEvent(events, item.getPickupDate().isBefore(fromDate) ? fromDate : item.getPickupDate(),
					item.getQuantity(), 0);
			addCapacityEvent(events, item.getReturnDate().isAfter(toDate) ? toDate : item.getReturnDate(),
					-item.getQuantity(), 0);
		}
		for (PreparedLine line : proposed) {
			addCapacityEvent(events, line.request().getPickupDate(), 0, line.request().getQuantity());
			addCapacityEvent(events, line.request().getReturnDate(), 0, -line.request().getQuantity());
		}
		long reserved = 0;
		long requested = 0;
		for (Map.Entry<LocalDate, CapacityEvent> event : events.entrySet()) {
			reserved += event.getValue().reservedDelta;
			requested += event.getValue().requestedDelta;
			if (requested > 0 && reserved + requested > readyUnits) {
				LocalDate conflictDate = event.getKey();
				for (PreparedLine line : proposed) {
					if (!conflictDate.isBefore(line.request().getPickupDate())
							&& conflictDate.isBefore(line.request().getReturnDate())) {
						conflicts.add(conflict(line, conflictDate,
								Math.toIntExact(Math.max(0L, readyUnits - reserved))));
					}
				}
				return;
			}
		}
	}

	private RentalInventory lockInventoryForItem(ReservationItem item) {
		Reservation reservation = item.getReservation();
		return rentalInventoryRepository.findForUpdate(reservation.getStoreId(), item.getListingId())
				.orElseThrow(() -> notFound("Reservation inventory not found"));
	}

	private ReservationItem lockStoreItem(UUID itemId, UUID storeId) {
		ReservationItem target = reservationItemRepository.findWithReservation(itemId)
				.filter(item -> item.getReservation().getStoreId().equals(storeId))
				.orElseThrow(() -> notFound("Reservation item not found"));
		lockInventoryForItem(target);
		return reservationItemRepository.findForStoreUpdate(itemId, storeId)
				.orElseThrow(() -> notFound("Reservation item not found"));
	}

	private String normalizeOptional(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private AvailabilityConflictResponse conflict(PreparedLine line, LocalDate date, int remaining) {
		return AvailabilityConflictResponse.builder()
				.itemIndex(line.index())
				.storeId(line.request().getStoreId())
				.listingId(line.request().getListingId())
				.date(date)
				.requestedQuantity(line.request().getQuantity())
				.remainingQuantity(remaining)
				.build();
	}

	private BigDecimal lineTotal(PreparedLine line) {
		long days = ChronoUnit.DAYS.between(line.request().getPickupDate(), line.request().getReturnDate());
		return line.listing().getDailyPrice().multiply(BigDecimal.valueOf(days))
				.multiply(BigDecimal.valueOf(line.request().getQuantity()));
	}

	private void addEvent(Map<LocalDate, Long> events, LocalDate date, long delta) {
		events.merge(date, delta, Long::sum);
	}

	private void addCapacityEvent(Map<LocalDate, CapacityEvent> events, LocalDate date,
			int reservedDelta, int requestedDelta) {
		CapacityEvent event = events.computeIfAbsent(date, ignored -> new CapacityEvent());
		event.reservedDelta += reservedDelta;
		event.requestedDelta += requestedDelta;
	}

	private RentalException notFound(String message) {
		return new RentalException(HttpStatus.NOT_FOUND, message);
	}

	private RentalException invalidRequest(String message) {
		return new RentalException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message, List.of(), List.of());
	}

	private RentalException conflict(String code, String message) {
		return new RentalException(HttpStatus.CONFLICT, code, message, List.of(), List.of());
	}

	public record PreparedLine(int index, ReservationItemRequest request, CatalogListingResponse listing) { }

	private record ListingKey(UUID storeId, UUID listingId) { }

	private static class CapacityEvent {
		private long reservedDelta;
		private long requestedDelta;
	}

}
