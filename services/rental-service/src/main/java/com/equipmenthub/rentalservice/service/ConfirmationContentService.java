package com.equipmenthub.rentalservice.service;

import com.equipmenthub.rentalservice.dto.response.ConfirmationContent;
import com.equipmenthub.rentalservice.entity.RentalCheckout;
import com.equipmenthub.rentalservice.entity.Reservation;
import com.equipmenthub.rentalservice.entity.ReservationItem;
import com.equipmenthub.rentalservice.repository.RentalCheckoutRepository;
import com.equipmenthub.rentalservice.repository.ReservationItemRepository;
import com.equipmenthub.rentalservice.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConfirmationContentService {

	@Autowired
	private RentalCheckoutRepository checkoutRepository;

	@Autowired
	private ReservationRepository reservationRepository;

	@Autowired
	private ReservationItemRepository itemRepository;

	@Transactional(readOnly = true)
	public ConfirmationContent build(UUID checkoutId) {
		RentalCheckout checkout = checkoutRepository.findById(checkoutId)
				.orElseThrow(() -> new IllegalArgumentException("Checkout does not exist"));
		List<Reservation> reservations = reservationRepository.findAllWithCheckoutByCheckoutId(checkoutId);
		if (reservations.isEmpty()) {
			throw new IllegalArgumentException("Checkout has no reservations");
		}
		List<ReservationItem> items = itemRepository.findAllByReservation_IdInOrderByPickupDateAscIdAsc(
				reservations.stream().map(Reservation::getId).toList());
		if (items.isEmpty() || items.size() > 100) {
			throw new IllegalArgumentException("Invalid confirmation item count");
		}
		var byReservation = items.stream().collect(Collectors.groupingBy(item -> item.getReservation().getId()));
		List<ConfirmationContent.StoreSection> stores = new ArrayList<>();
		for (Reservation reservation : reservations) {
			List<ReservationItem> storeItems = byReservation.getOrDefault(reservation.getId(), List.of());
			BigDecimal sum = storeItems.stream().map(ReservationItem::getTotalAmount)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			if (storeItems.isEmpty() || sum.compareTo(reservation.getTotalAmount()) != 0) {
				throw new IllegalArgumentException("Reservation totals do not match");
			}
			stores.add(ConfirmationContent.StoreSection.builder().storeId(reservation.getStoreId())
					.reservationId(reservation.getId()).totalAmount(reservation.getTotalAmount())
					.items(storeItems.stream().map(this::line).toList()).build());
		}
		BigDecimal total = stores.stream().map(ConfirmationContent.StoreSection::getTotalAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		if (total.compareTo(checkout.getTotalAmount()) != 0) {
			throw new IllegalArgumentException("Checkout totals do not match");
		}
		return ConfirmationContent.builder().checkoutId(checkoutId).reservedAt(checkout.getCreatedAt())
				.preparedAt(Instant.now()).timeZone("Europe/Istanbul").currencyCode(checkout.getCurrencyCode())
				.totalAmount(checkout.getTotalAmount()).stores(stores).build();
	}

	private ConfirmationContent.Line line(ReservationItem item) {
		long days = ChronoUnit.DAYS.between(item.getPickupDate(), item.getReturnDate());
		BigDecimal expected = item.getDailyPrice().multiply(BigDecimal.valueOf(days))
				.multiply(BigDecimal.valueOf(item.getQuantity()));
		if (days < 1 || item.getQuantity() < 1 || expected.compareTo(item.getTotalAmount()) != 0) {
			throw new IllegalArgumentException("Invalid confirmation line");
		}
		return ConfirmationContent.Line.builder().itemId(item.getId()).listingId(item.getListingId())
				.listingName(item.getListingName()).quantity(item.getQuantity()).rentalDays(days)
				.pickupAt(item.getPickupDate().atTime(LocalTime.NOON).atZone(ZoneId.of("Europe/Istanbul")).toOffsetDateTime())
				.returnAt(item.getReturnDate().atTime(9, 0).atZone(ZoneId.of("Europe/Istanbul")).toOffsetDateTime())
				.dailyPrice(item.getDailyPrice()).totalAmount(item.getTotalAmount()).build();
	}

}
