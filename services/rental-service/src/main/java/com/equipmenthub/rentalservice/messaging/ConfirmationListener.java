package com.equipmenthub.rentalservice.messaging;

import com.equipmenthub.rentalservice.repository.RentalOutboxEventRepository;
import com.equipmenthub.rentalservice.service.ConfirmationPersistenceService;
import com.equipmenthub.rentalservice.service.ConfirmationPreparationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConfirmationListener {

	@Autowired
	private CheckoutEventCodec eventCodec;

	@Autowired
	private ConfirmationPersistenceService persistenceService;

	@Autowired
	private ConfirmationPreparationService preparationService;

	@Autowired
	private RentalOutboxEventRepository outboxRepository;

	@KafkaListener(topics = RentalKafkaTopics.CHECKOUT_CONFIRMED, groupId = "rental-confirmation-v1",
			autoStartup = "${equipmenthub.confirmation.consumer-enabled:true}")
	public void onCheckout(ConsumerRecord<String, String> record) {
		CheckoutConfirmedEvent event = eventCodec.read(record.value());
		if (!event.getCheckoutId().toString().equals(record.key())) {
			throw new IllegalArgumentException("Checkout event key does not match");
		}
		if (persistenceService.isReady(event.getCheckoutId())) {
			return;
		}
		if (!outboxRepository.existsByIdAndCheckout_Id(event.getEventId(), event.getCheckoutId())) {
			throw new IllegalArgumentException("Checkout event does not match a recorded job");
		}
		preparationService.prepare(event.getCheckoutId());
		log.info("Confirmation prepared checkoutId={} eventId={}", event.getCheckoutId(), event.getEventId());
	}

}
