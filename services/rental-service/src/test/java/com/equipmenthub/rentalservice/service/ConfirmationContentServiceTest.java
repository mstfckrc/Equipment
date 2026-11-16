package com.equipmenthub.rentalservice.service;

import com.equipmenthub.rentalservice.entity.*;
import com.equipmenthub.rentalservice.repository.RentalCheckoutRepository;
import com.equipmenthub.rentalservice.repository.ReservationItemRepository;
import com.equipmenthub.rentalservice.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmationContentServiceTest {

	@Mock private RentalCheckoutRepository checkoutRepository;
	@Mock private ReservationRepository reservationRepository;
	@Mock private ReservationItemRepository itemRepository;
	@InjectMocks private ConfirmationContentService service;

	private RentalCheckout checkout;
	private Reservation reservation;
	private ReservationItem item;

	@BeforeEach
	void sourceData() {
		checkout = new RentalCheckout();
		checkout.setId(UUID.randomUUID());
		checkout.setCreatedAt(Instant.parse("2026-11-16T09:00:00Z"));
		checkout.setTotalAmount(new BigDecimal("600.00"));
		reservation = new Reservation();
		reservation.setId(UUID.randomUUID());
		reservation.setCheckout(checkout);
		reservation.setStoreId(UUID.randomUUID());
		reservation.setTotalAmount(new BigDecimal("600.00"));
		item = new ReservationItem();
		item.setId(UUID.randomUUID());
		item.setReservation(reservation);
		item.setListingId(UUID.randomUUID());
		item.setListingName("Onaylanan kamera adı");
		item.setQuantity(2);
		item.setPickupDate(LocalDate.of(2027, 2, 1));
		item.setReturnDate(LocalDate.of(2027, 2, 4));
		item.setDailyPrice(new BigDecimal("100.00"));
		item.setTotalAmount(new BigDecimal("600.00"));
		when(checkoutRepository.findById(checkout.getId())).thenReturn(Optional.of(checkout));
		when(reservationRepository.findAllWithCheckoutByCheckoutId(checkout.getId())).thenReturn(List.of(reservation));
		when(itemRepository.findAllByReservation_IdInOrderByPickupDateAscIdAsc(List.of(reservation.getId())))
				.thenReturn(List.of(item));
	}

	@Test
	void preservesAcceptedValuesEvenAfterCancellation() {
		item.setStatus(ReservationItemStatus.CANCELLED);
		var content = service.build(checkout.getId());
		var line = content.getStores().getFirst().getItems().getFirst();
		assertThat(content.getReservedAt()).isEqualTo(checkout.getCreatedAt());
		assertThat(content.getTotalAmount()).isEqualByComparingTo("600.00");
		assertThat(line.getListingName()).isEqualTo("Onaylanan kamera adı");
		assertThat(line.getRentalDays()).isEqualTo(3);
		assertThat(line.getQuantity()).isEqualTo(2);
		assertThat(line.getPickupAt().getHour()).isEqualTo(12);
		assertThat(line.getReturnAt().getHour()).isEqualTo(9);
		assertThat(line.getPickupAt().getOffset()).isEqualTo(ZoneOffset.ofHours(3));
	}

	@Test
	void refusesAnInconsistentStoredTotal() {
		item.setDailyPrice(new BigDecimal("101.00"));
		assertThatThrownBy(() -> service.build(checkout.getId())).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void keepsDifferentDatesOfSameProductAsSeparateLines() {
		ReservationItem second = new ReservationItem();
		second.setId(UUID.randomUUID());
		second.setReservation(reservation);
		second.setListingId(item.getListingId());
		second.setListingName(item.getListingName());
		second.setQuantity(1);
		second.setPickupDate(LocalDate.of(2027, 2, 10));
		second.setReturnDate(LocalDate.of(2027, 2, 11));
		second.setDailyPrice(new BigDecimal("100.00"));
		second.setTotalAmount(new BigDecimal("100.00"));
		reservation.setTotalAmount(new BigDecimal("700.00"));
		checkout.setTotalAmount(new BigDecimal("700.00"));
		when(itemRepository.findAllByReservation_IdInOrderByPickupDateAscIdAsc(List.of(reservation.getId())))
				.thenReturn(List.of(item, second));
		var lines = service.build(checkout.getId()).getStores().getFirst().getItems();
		assertThat(lines).hasSize(2);
		assertThat(lines.get(0).getPickupAt()).isNotEqualTo(lines.get(1).getPickupAt());
		assertThat(lines).extracting(line -> line.getQuantity()).containsExactly(2, 1);
	}

}
