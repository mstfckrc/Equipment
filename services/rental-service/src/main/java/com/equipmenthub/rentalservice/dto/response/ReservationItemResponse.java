package com.equipmenthub.rentalservice.dto.response;

import com.equipmenthub.rentalservice.entity.ReservationItem;
import com.equipmenthub.rentalservice.entity.ReservationItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationItemResponse {

	private static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Istanbul");

	private UUID itemId;
	private UUID listingId;
	private String listingName;
	private int quantity;
	private LocalDate pickupDate;
	private LocalDate returnDate;
	private LocalTime pickupTime;
	private LocalTime returnTime;
	private BigDecimal dailyPrice;
	private BigDecimal totalAmount;
	private String currencyCode;
	private ReservationItemStatus status;
	private boolean pickupMissed;
	private boolean overdue;
	private boolean returnedLate;
	private List<ReservationUnitAssignmentResponse> assignedUnits;

	public static ReservationItemResponse from(ReservationItem item) {
		return from(item, List.of(), LocalDateTime.now(BUSINESS_ZONE));
	}

	public static ReservationItemResponse from(
			ReservationItem item, List<ReservationUnitAssignmentResponse> assignedUnits, LocalDateTime now) {
		LocalDateTime pickupAt = item.getPickupDate().atTime(LocalTime.NOON);
		LocalDateTime dueAt = item.getReturnDate().atTime(LocalTime.of(9, 0));
		boolean pickupMissed = item.getStatus() == ReservationItemStatus.CONFIRMED && now.isAfter(pickupAt);
		boolean overdue = (item.getStatus() == ReservationItemStatus.PICKED_UP
				|| item.getStatus() == ReservationItemStatus.RETURN_REQUESTED) && now.isAfter(dueAt);
		boolean returnedLate = assignedUnits.stream().map(ReservationUnitAssignmentResponse::getReceivedAt)
				.filter(receivedAt -> receivedAt != null)
				.anyMatch(receivedAt -> receivedAt.isAfter(dueAt.atZone(BUSINESS_ZONE).toInstant()));
		return ReservationItemResponse.builder()
				.itemId(item.getId())
				.listingId(item.getListingId())
				.listingName(item.getListingName())
				.quantity(item.getQuantity())
				.pickupDate(item.getPickupDate())
				.returnDate(item.getReturnDate())
				.pickupTime(LocalTime.NOON)
				.returnTime(LocalTime.of(9, 0))
				.dailyPrice(item.getDailyPrice())
				.totalAmount(item.getTotalAmount())
				.currencyCode(item.getCurrencyCode())
				.status(item.getStatus())
				.pickupMissed(pickupMissed)
				.overdue(overdue)
				.returnedLate(returnedLate)
				.assignedUnits(assignedUnits)
				.build();
	}

}
