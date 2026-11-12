package com.equipmenthub.rentalservice.service;

import com.equipmenthub.rentalservice.client.CatalogServiceClient;
import com.equipmenthub.rentalservice.client.StoreServiceClient;
import com.equipmenthub.rentalservice.dto.request.ReservationItemStatusRequest;
import com.equipmenthub.rentalservice.dto.request.ReservationCheckoutRequest;
import com.equipmenthub.rentalservice.dto.request.ReservationItemRequest;
import com.equipmenthub.rentalservice.dto.response.AvailabilityCalendarResponse;
import com.equipmenthub.rentalservice.dto.response.CatalogListingResponse;
import com.equipmenthub.rentalservice.dto.response.PriceChangeResponse;
import com.equipmenthub.rentalservice.dto.response.ReservationCheckoutResponse;
import com.equipmenthub.rentalservice.dto.response.ReservationItemResponse;
import com.equipmenthub.rentalservice.dto.response.ReservationResponse;
import com.equipmenthub.rentalservice.exception.RentalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RentalReservationService {

	private static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Istanbul");
	private static final int MAX_CALENDAR_WINDOW_DAYS = 62;

	@Autowired
	private CatalogServiceClient catalogServiceClient;

	@Autowired
	private StoreServiceClient storeServiceClient;

	@Autowired
	private RentalReservationPersistenceService rentalReservationPersistenceService;

	public AvailabilityCalendarResponse getAvailability(
			UUID storeId, UUID listingId, LocalDate fromDate, LocalDate toDateExclusive, String accessToken) {
		if (fromDate == null || toDateExclusive == null || !toDateExclusive.isAfter(fromDate)
				|| ChronoUnit.DAYS.between(fromDate, toDateExclusive) > MAX_CALENDAR_WINDOW_DAYS) {
			throw invalidRequest("Choose a valid calendar date range of up to 62 days");
		}
		CatalogListingResponse listing = catalogServiceClient.getPublicListing(storeId, listingId, accessToken);
		return rentalReservationPersistenceService.getAvailability(
				storeId, listingId, listing.getName(), listing.getDailyPrice(), fromDate, toDateExclusive);
	}

	public ReservationCheckoutResponse checkout(
			ReservationCheckoutRequest request, String idempotencyKey, String accessToken, String customerSubject) {
		UUID customerId = parseCustomerId(customerSubject);
		if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 100) {
			throw invalidRequest("A valid Idempotency-Key header is required");
		}
		String fingerprint = fingerprint(request);

		ReservationCheckoutResponse existing = rentalReservationPersistenceService
				.findCheckout(customerId, idempotencyKey, fingerprint);
		if (existing != null) {
			return existing;
		}
		validateDates(request.getItems(), LocalDate.now(BUSINESS_ZONE));

		Map<ListingKey, CatalogListingResponse> listings = new LinkedHashMap<>();
		List<RentalReservationPersistenceService.PreparedLine> preparedLines = new ArrayList<>();
		List<PriceChangeResponse> priceChanges = new ArrayList<>();
		for (int index = 0; index < request.getItems().size(); index++) {
			ReservationItemRequest item = request.getItems().get(index);
			ListingKey key = new ListingKey(item.getStoreId(), item.getListingId());
			CatalogListingResponse listing = listings.computeIfAbsent(key,
					ignored -> catalogServiceClient.getPublicListing(item.getStoreId(), item.getListingId(), accessToken));
			if (item.getExpectedDailyPrice().compareTo(listing.getDailyPrice()) != 0) {
				BigDecimal currentLineTotal = lineTotal(listing.getDailyPrice(), item);
				priceChanges.add(PriceChangeResponse.builder()
						.itemIndex(index)
						.storeId(item.getStoreId())
						.listingId(item.getListingId())
						.listingName(listing.getName())
						.previouslyShownDailyPrice(item.getExpectedDailyPrice())
						.previouslyShownLineTotal(lineTotal(item.getExpectedDailyPrice(), item))
						.currentDailyPrice(listing.getDailyPrice())
						.currentLineTotal(currentLineTotal)
						.currencyCode("TRY")
						.build());
			}
			preparedLines.add(new RentalReservationPersistenceService.PreparedLine(index, item, listing));
		}
		if (!priceChanges.isEmpty()) {
			throw new RentalException(HttpStatus.CONFLICT, "PRICE_CHANGED",
					"Some listing prices changed. Review the updated amounts and confirm again.", priceChanges, List.of());
		}
		return rentalReservationPersistenceService.createCheckout(
				customerId, idempotencyKey, fingerprint, preparedLines);
	}

	public List<ReservationResponse> getCustomerReservations(String customerSubject) {
		return rentalReservationPersistenceService.getCustomerReservations(parseCustomerId(customerSubject));
	}

	public List<ReservationResponse> getStoreReservations(String accessToken) {
		UUID storeId = storeServiceClient.getActiveStoreId(accessToken);
		return rentalReservationPersistenceService.getStoreReservations(storeId);
	}

	public ReservationItemResponse cancelCustomerItem(UUID itemId, String customerSubject) {
		return rentalReservationPersistenceService.cancelCustomerItem(
				itemId, parseCustomerId(customerSubject), LocalDateTime.now(BUSINESS_ZONE));
	}

	public ReservationItemResponse cancelStoreItem(UUID itemId, String accessToken) {
		UUID storeId = storeServiceClient.getActiveStoreId(accessToken);
		return rentalReservationPersistenceService.cancelStoreItem(itemId, storeId, LocalDateTime.now(BUSINESS_ZONE));
	}

	public ReservationItemResponse requestCustomerReturn(UUID itemId, String customerSubject) {
		return rentalReservationPersistenceService.requestCustomerReturn(
				itemId, parseCustomerId(customerSubject), LocalDateTime.now(BUSINESS_ZONE));
	}

	public ReservationItemResponse handoverStoreItem(
			UUID itemId, ReservationItemStatusRequest request, String accessToken) {
		UUID storeId = storeServiceClient.getActiveStoreId(accessToken);
		return rentalReservationPersistenceService.handoverStoreItem(
				itemId, storeId, request.getUnitIds(), LocalDateTime.now(BUSINESS_ZONE));
	}

	public ReservationItemResponse receiveStoreReturn(UUID itemId, String accessToken) {
		UUID storeId = storeServiceClient.getActiveStoreId(accessToken);
		return rentalReservationPersistenceService.receiveStoreReturn(
				itemId, storeId, LocalDateTime.now(BUSINESS_ZONE));
	}

	public ReservationItemResponse inspectStoreReturn(
			UUID itemId, ReservationItemStatusRequest request, String accessToken) {
		UUID storeId = storeServiceClient.getActiveStoreId(accessToken);
		return rentalReservationPersistenceService.inspectStoreReturn(
				itemId, storeId, request.getUnitInspections(), LocalDateTime.now(BUSINESS_ZONE));
	}

	private void validateDates(List<ReservationItemRequest> items, LocalDate today) {
		for (ReservationItemRequest item : items) {
			if (!item.getPickupDate().isAfter(today)) {
				throw invalidRequest("Pickup dates must be in the future");
			}
			if (!item.getReturnDate().isAfter(item.getPickupDate())) {
				throw invalidRequest("Return date must be after pickup date");
			}
		}
	}

	private BigDecimal lineTotal(BigDecimal dailyPrice, ReservationItemRequest item) {
		long days = ChronoUnit.DAYS.between(item.getPickupDate(), item.getReturnDate());
		return dailyPrice.multiply(BigDecimal.valueOf(days)).multiply(BigDecimal.valueOf(item.getQuantity()));
	}

	private UUID parseCustomerId(String subject) {
		try {
			return UUID.fromString(subject);
		}
		catch (RuntimeException exception) {
			throw new RentalException(HttpStatus.UNAUTHORIZED, "A valid customer identity is required");
		}
	}

	private String fingerprint(ReservationCheckoutRequest request) {
		StringBuilder canonical = new StringBuilder();
		for (ReservationItemRequest item : request.getItems()) {
			canonical.append(item.getStoreId()).append('|')
					.append(item.getListingId()).append('|')
					.append(item.getQuantity()).append('|')
					.append(item.getPickupDate()).append('|')
					.append(item.getReturnDate()).append('|')
					.append(item.getExpectedDailyPrice().stripTrailingZeros().toPlainString()).append('\n');
		}
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
					.digest(canonical.toString().getBytes(StandardCharsets.UTF_8)));
		}
		catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is unavailable", exception);
		}
	}

	private RentalException invalidRequest(String message) {
		return new RentalException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message, List.of(), List.of());
	}

	private record ListingKey(UUID storeId, UUID listingId) { }

}
