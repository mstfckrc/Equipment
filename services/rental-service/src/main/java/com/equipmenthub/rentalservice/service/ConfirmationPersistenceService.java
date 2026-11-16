package com.equipmenthub.rentalservice.service;

import com.equipmenthub.rentalservice.dto.response.ConfirmationContent;
import com.equipmenthub.rentalservice.dto.response.ConfirmationResponse;
import com.equipmenthub.rentalservice.entity.ConfirmationStatus;
import com.equipmenthub.rentalservice.entity.RentalCheckout;
import com.equipmenthub.rentalservice.entity.RentalConfirmation;
import com.equipmenthub.rentalservice.entity.RentalOutboxEvent;
import com.equipmenthub.rentalservice.exception.RentalException;
import com.equipmenthub.rentalservice.repository.RentalCheckoutRepository;
import com.equipmenthub.rentalservice.repository.RentalConfirmationRepository;
import com.equipmenthub.rentalservice.repository.RentalOutboxEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ConfirmationPersistenceService {

	@Autowired
	private RentalCheckoutRepository checkoutRepository;

	@Autowired
	private RentalConfirmationRepository confirmationRepository;

	@Autowired
	private RentalOutboxEventRepository outboxRepository;

	@Transactional
	public void enqueue(RentalCheckout checkout) {
		RentalConfirmation confirmation = new RentalConfirmation();
		confirmation.setCheckout(checkout);
		confirmationRepository.save(confirmation);
		RentalOutboxEvent event = outboxRepository.findByCheckout_Id(checkout.getId()).orElseGet(RentalOutboxEvent::new);
		event.setCheckout(checkout);
		event.setOccurredAt(checkout.getCreatedAt());
		event.setPublishedAt(null);
		event.setNextAttemptAt(Instant.now());
		outboxRepository.save(event);
	}

	@Transactional
	public void enqueueMissing(UUID checkoutId) {
		RentalCheckout checkout = checkoutRepository.findForUpdate(checkoutId).orElse(null);
		if (checkout != null && !confirmationRepository.existsByCheckout_Id(checkoutId)) {
			enqueue(checkout);
		}
	}

	@Transactional(readOnly = true)
	public ConfirmationResponse getOwnConfirmation(UUID checkoutId, UUID customerId) {
		checkoutRepository.findByIdAndCustomerId(checkoutId, customerId)
				.orElseThrow(() -> new RentalException(HttpStatus.NOT_FOUND, "Onay belgesi bulunamadı."));
		RentalConfirmation confirmation = confirmationRepository.findByCheckout_Id(checkoutId).orElse(null);
		if (confirmation == null) {
			// Historical checkouts are queued by the background backfill, never by GET.
			return ConfirmationResponse.builder().checkoutId(checkoutId).contentVersion(1)
					.status(ConfirmationStatus.PENDING).message("Onay belgesi sıraya alınıyor.").build();
		}
		return ConfirmationResponse.builder().documentId(confirmation.getId()).checkoutId(checkoutId)
				.status(confirmation.getStatus()).contentVersion(confirmation.getContentVersion())
				.preparedAt(confirmation.getPreparedAt()).errorCode(confirmation.getErrorCode())
				.message(switch (confirmation.getStatus()) {
					case PENDING -> "Onay belgesi hazırlanıyor.";
					case READY -> "Onay belgesi hazır.";
					case FAILED -> "Onay belgesi şu anda hazırlanamadı. Rezervasyonun geçerliliği etkilenmedi.";
				})
				.content(confirmation.getStatus() == ConfirmationStatus.READY ? confirmation.getContent() : null)
				.build();
	}

	@Transactional(readOnly = true)
	public boolean isReady(UUID checkoutId) {
		return confirmationRepository.findByCheckout_Id(checkoutId)
				.map(value -> value.getStatus() == ConfirmationStatus.READY).orElse(false);
	}

	@Transactional
	public void saveReady(UUID checkoutId, ConfirmationContent content) {
		RentalConfirmation confirmation = confirmationRepository.findForUpdate(checkoutId)
				.orElseThrow(() -> new IllegalArgumentException("Confirmation job does not exist"));
		if (confirmation.getStatus() == ConfirmationStatus.READY) {
			return;
		}
		confirmation.setContent(content);
		confirmation.setPreparedAt(content.getPreparedAt());
		confirmation.setErrorCode(null);
		confirmation.setStatus(ConfirmationStatus.READY);
	}

	@Transactional
	public void markFailed(UUID checkoutId) {
		confirmationRepository.findForUpdate(checkoutId).ifPresent(confirmation -> {
			if (confirmation.getStatus() != ConfirmationStatus.READY) {
				confirmation.setStatus(ConfirmationStatus.FAILED);
				confirmation.setErrorCode("CONFIRMATION_PREPARATION_FAILED");
			}
		});
	}

}
