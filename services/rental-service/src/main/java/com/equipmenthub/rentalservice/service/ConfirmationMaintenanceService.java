package com.equipmenthub.rentalservice.service;

import com.equipmenthub.rentalservice.repository.RentalCheckoutRepository;
import com.equipmenthub.rentalservice.entity.ConfirmationStatus;
import com.equipmenthub.rentalservice.repository.RentalOutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(name = "equipmenthub.confirmation.maintenance-enabled", havingValue = "true", matchIfMissing = true)
public class ConfirmationMaintenanceService {

	@Autowired
	private RentalCheckoutRepository checkoutRepository;

	@Autowired
	private RentalOutboxEventRepository outboxRepository;

	@Autowired
	private ConfirmationPersistenceService persistenceService;

	@Autowired
	private RentalOutboxDeliveryService deliveryService;

	@Scheduled(fixedDelayString = "${equipmenthub.confirmation.publish-delay-ms:1000}", initialDelay = 3000)
	public void publishPending() {
		try {
			for (UUID id : outboxRepository.findDueIds(Instant.now(), PageRequest.of(0, 10))) {
				if (!deliveryService.deliver(id)) {
					break;
				}
			}
		}
		catch (RuntimeException exception) {
			log.warn("Confirmation outbox temporarily unavailable");
		}
	}

	@Scheduled(fixedDelay = 30000, initialDelay = 1000)
	public void backfillAndRecover() {
		try {
			for (UUID id : checkoutRepository.findWithoutConfirmation(PageRequest.of(0, 25))) {
				persistenceService.enqueueMissing(id);
			}
			for (UUID id : outboxRepository.findPublishedIds(ConfirmationStatus.PENDING, Instant.now().minus(15, ChronoUnit.MINUTES),
					PageRequest.of(0, 25))) {
				deliveryService.requeue(id);
			}
		}
		catch (RuntimeException exception) {
			log.warn("Confirmation backfill/recovery will retry");
		}
	}

	@Scheduled(fixedDelay = 3600000, initialDelay = 60000)
	public void cleanCompletedOutbox() {
		try {
			var ids = outboxRepository.findPublishedIds(ConfirmationStatus.READY, Instant.now().minus(7, ChronoUnit.DAYS),
					PageRequest.of(0, 100));
			if (!ids.isEmpty()) {
				outboxRepository.deleteAllByIdInBatch(ids);
			}
		}
		catch (RuntimeException exception) {
			log.warn("Completed confirmation outbox cleanup will retry");
		}
	}

}
