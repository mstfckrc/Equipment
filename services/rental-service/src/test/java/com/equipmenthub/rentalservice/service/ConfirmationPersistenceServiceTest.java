package com.equipmenthub.rentalservice.service;

import com.equipmenthub.rentalservice.dto.response.ConfirmationContent;
import com.equipmenthub.rentalservice.entity.ConfirmationStatus;
import com.equipmenthub.rentalservice.entity.RentalConfirmation;
import com.equipmenthub.rentalservice.entity.RentalCheckout;
import com.equipmenthub.rentalservice.exception.RentalException;
import com.equipmenthub.rentalservice.repository.RentalCheckoutRepository;
import com.equipmenthub.rentalservice.repository.RentalConfirmationRepository;
import com.equipmenthub.rentalservice.repository.RentalOutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmationPersistenceServiceTest {

	@Mock private RentalCheckoutRepository checkoutRepository;
	@Mock private RentalConfirmationRepository confirmationRepository;
	@Mock private RentalOutboxEventRepository outboxRepository;
	@InjectMocks private ConfirmationPersistenceService service;

	@Test
	void returnsNotFoundForAnotherCustomersCheckoutWithoutReadingTheDocument() {
		UUID checkoutId = UUID.randomUUID();
		UUID otherCustomer = UUID.randomUUID();
		when(checkoutRepository.findByIdAndCustomerId(checkoutId, otherCustomer)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> service.getOwnConfirmation(checkoutId, otherCustomer))
				.isInstanceOfSatisfying(RentalException.class,
						error -> assertThat(error.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
		verifyNoInteractions(confirmationRepository, outboxRepository);
	}

	@Test
	void readingAnOlderCheckoutNeverCreatesAJob() {
		UUID checkoutId = UUID.randomUUID();
		UUID customerId = UUID.randomUUID();
		when(checkoutRepository.findByIdAndCustomerId(checkoutId, customerId)).thenReturn(Optional.of(new RentalCheckout()));
		when(confirmationRepository.findByCheckout_Id(checkoutId)).thenReturn(Optional.empty());
		assertThat(service.getOwnConfirmation(checkoutId, customerId).getStatus()).isEqualTo(ConfirmationStatus.PENDING);
		verify(confirmationRepository, never()).save(any());
		verifyNoInteractions(outboxRepository);
	}

	@Test
	void duplicateOrLateFailureCannotOverwriteReadyContent() {
		UUID checkoutId = UUID.randomUUID();
		RentalConfirmation confirmation = new RentalConfirmation();
		ConfirmationContent original = new ConfirmationContent();
		confirmation.setStatus(ConfirmationStatus.READY);
		confirmation.setContent(original);
		when(confirmationRepository.findForUpdate(checkoutId)).thenReturn(Optional.of(confirmation));
		service.saveReady(checkoutId, new ConfirmationContent());
		service.markFailed(checkoutId);
		assertThat(confirmation.getContent()).isSameAs(original);
		assertThat(confirmation.getStatus()).isEqualTo(ConfirmationStatus.READY);
		assertThat(confirmation.getErrorCode()).isNull();
	}

}
