package com.equipmenthub.rentalservice.service;

import com.equipmenthub.rentalservice.dto.request.ReservationItemRequest;
import com.equipmenthub.rentalservice.dto.response.CatalogListingResponse;
import com.equipmenthub.rentalservice.entity.EquipmentUnitStatus;
import com.equipmenthub.rentalservice.entity.RentalInventory;
import com.equipmenthub.rentalservice.entity.ReservationItemStatus;
import com.equipmenthub.rentalservice.exception.RentalException;
import com.equipmenthub.rentalservice.repository.EquipmentUnitRepository;
import com.equipmenthub.rentalservice.repository.RentalCheckoutRepository;
import com.equipmenthub.rentalservice.repository.RentalInventoryRepository;
import com.equipmenthub.rentalservice.repository.ReservationInterval;
import com.equipmenthub.rentalservice.repository.ReservationItemRepository;
import com.equipmenthub.rentalservice.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalReservationPersistenceServiceTest {

	@Mock private RentalInventoryRepository inventories;
	@Mock private EquipmentUnitRepository units;
	@Mock private ReservationItemRepository items;
	@Mock private RentalCheckoutRepository checkouts;
	@Mock private ReservationRepository reservations;
	@Mock private ConfirmationPersistenceService confirmations;
	@InjectMocks private RentalReservationPersistenceService service;

	private final UUID storeId = UUID.randomUUID();
	private final UUID listingId = UUID.randomUUID();
	private final UUID customerId = UUID.randomUUID();
	private final BigDecimal dailyPrice = new BigDecimal("100.00");
	private final LocalDate start = LocalDate.now(ZoneId.of("Europe/Istanbul")).plusDays(30);
	private RentalInventory inventory;

	@BeforeEach
	void inventoryRecord() {
		inventory = new RentalInventory();
		inventory.setId(UUID.randomUUID());
		inventory.setStoreId(storeId);
		inventory.setListingId(listingId);
	}

	@Test
	void calendarReleasesReservedCapacityOnTheReturnDay() {
		ReservationInterval existing = interval(start, start.plusDays(2), 2);
		when(inventories.findByStoreIdAndListingId(storeId, listingId)).thenReturn(Optional.of(inventory));
		when(units.countByInventory_IdAndStatus(inventory.getId(), EquipmentUnitStatus.READY)).thenReturn(2L);
		when(items.findOverlapping(listingId, ReservationItemStatus.CONFIRMED, start, start.plusDays(3)))
				.thenReturn(List.of(existing));

		var calendar = service.getAvailability(storeId, listingId, "Test kamera", dailyPrice, start, start.plusDays(3));

		assertEquals(List.of(0, 0, 2), calendar.getDays().stream().map(day -> day.getRemainingUnits()).toList());
		assertEquals(List.of(2, 2, 0), calendar.getDays().stream().map(day -> day.getReservedUnits()).toList());
	}

	@Test
	void rejectsARequestExceedingCapacityAlreadyReservedForThoseDates() {
		ReservationInterval existing = interval(start, start.plusDays(2), 2);
		checkoutInventory(3);
		when(items.findOverlapping(listingId, ReservationItemStatus.CONFIRMED, start.plusDays(1), start.plusDays(3)))
				.thenReturn(List.of(existing));

		RentalException error = assertThrows(RentalException.class, () -> service.createCheckout(
				customerId, "capacity-check", "fingerprint", List.of(line(0, start.plusDays(1), start.plusDays(3), 2))));

		assertEquals(HttpStatus.CONFLICT, error.getStatus());
		assertEquals("AVAILABILITY_CHANGED", error.getCode());
		assertEquals(start.plusDays(1), error.getAvailabilityConflicts().getFirst().getDate());
		assertEquals(1, error.getAvailabilityConflicts().getFirst().getRemainingQuantity());
		verify(checkouts, never()).saveAndFlush(any());
		verifyNoInteractions(reservations, confirmations);
	}

	@Test
	void combinesOverlappingCartLinesBeforeCheckingTheStockLimit() {
		checkoutInventory(3);
		when(items.findOverlapping(listingId, ReservationItemStatus.CONFIRMED, start, start.plusDays(3)))
				.thenReturn(List.of());

		RentalException error = assertThrows(RentalException.class, () -> service.createCheckout(
				customerId, "capacity-check", "fingerprint", List.of(
						line(0, start, start.plusDays(2), 2), line(1, start.plusDays(1), start.plusDays(3), 2))));

		assertEquals("AVAILABILITY_CHANGED", error.getCode());
		assertEquals(List.of(0, 1), error.getAvailabilityConflicts().stream().map(conflict -> conflict.getItemIndex()).toList());
		verify(checkouts, never()).saveAndFlush(any());
		verifyNoInteractions(reservations, confirmations);
	}

	private void checkoutInventory(long readyUnits) {
		when(checkouts.findByCustomerIdAndIdempotencyKey(customerId, "capacity-check")).thenReturn(Optional.empty());
		when(inventories.findForUpdate(storeId, listingId)).thenReturn(Optional.of(inventory));
		when(units.countByInventory_IdAndStatus(inventory.getId(), EquipmentUnitStatus.READY)).thenReturn(readyUnits);
	}

	private ReservationInterval interval(LocalDate pickup, LocalDate returnDate, int quantity) {
		ReservationInterval interval = mock(ReservationInterval.class);
		when(interval.getPickupDate()).thenReturn(pickup);
		when(interval.getReturnDate()).thenReturn(returnDate);
		when(interval.getQuantity()).thenReturn(quantity);
		return interval;
	}

	private RentalReservationPersistenceService.PreparedLine line(int index, LocalDate pickup, LocalDate returnDate, int quantity) {
		ReservationItemRequest request = new ReservationItemRequest(storeId, listingId, quantity, pickup, returnDate, dailyPrice);
		CatalogListingResponse listing = new CatalogListingResponse(listingId, storeId, "Test kamera", dailyPrice);
		return new RentalReservationPersistenceService.PreparedLine(index, request, listing);
	}
}
