package com.equipmenthub.rentalservice.service;

import com.equipmenthub.rentalservice.client.CatalogServiceClient;
import com.equipmenthub.rentalservice.client.StoreServiceClient;
import com.equipmenthub.rentalservice.dto.request.ReservationCheckoutRequest;
import com.equipmenthub.rentalservice.dto.request.ReservationItemRequest;
import com.equipmenthub.rentalservice.dto.response.ReservationCheckoutResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalReservationServiceTest {

	@Mock
	private CatalogServiceClient catalog;

	@Mock
	private StoreServiceClient stores;

	@Mock
	private RentalReservationPersistenceService persistence;

	@InjectMocks
	private RentalReservationService service;

	@Test
	void repeatedCheckoutReturnsTheOriginalReservationWithoutCreatingAnother() {
		UUID customerId = UUID.randomUUID();
		LocalDate pickup = LocalDate.of(2026, 1, 10);
		ReservationItemRequest item = new ReservationItemRequest(UUID.randomUUID(), UUID.randomUUID(),
				1, pickup, pickup.plusDays(1), new BigDecimal("100.00"));
		ReservationCheckoutRequest request = new ReservationCheckoutRequest(List.of(item));
		ReservationCheckoutResponse original = ReservationCheckoutResponse.builder()
				.checkoutId(UUID.randomUUID()).totalAmount(new BigDecimal("100.00")).currencyCode("TRY").build();
		when(persistence.findCheckout(eq(customerId), eq("checkout-retry"), anyString())).thenReturn(original);

		var result = service.checkout(request, "checkout-retry", "unused-token", customerId.toString());

		assertSame(original, result);
		verifyNoInteractions(catalog, stores);
		verify(persistence, never()).createCheckout(any(), anyString(), anyString(), anyList());
	}
}
