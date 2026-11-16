package com.equipmenthub.rentalservice.service;

import com.equipmenthub.rentalservice.messaging.CheckoutConfirmedEvent;
import com.equipmenthub.rentalservice.messaging.RentalKafkaTopics;
import com.equipmenthub.rentalservice.entity.RentalOutboxEvent;
import com.equipmenthub.rentalservice.repository.RentalOutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RentalOutboxDeliveryService {

	@Autowired
	private RentalOutboxEventRepository outboxRepository;

	@Autowired
	private KafkaTemplate<Object, Object> kafkaTemplate;

	@Autowired
	private JsonMapper jsonMapper;

	@Transactional
	public boolean deliver(UUID id) {
		RentalOutboxEvent event = outboxRepository.findForUpdate(id).orElse(null);
		Instant now = Instant.now();
		if (event == null || event.getPublishedAt() != null || event.getNextAttemptAt().isAfter(now)) {
			return true;
		}
		try {
			CheckoutConfirmedEvent message = new CheckoutConfirmedEvent(event.getId(), RentalKafkaTopics.EVENT_TYPE,
					1, event.getCheckout().getId(), event.getOccurredAt());
			kafkaTemplate.send(RentalKafkaTopics.CHECKOUT_CONFIRMED, message.getCheckoutId().toString(),
					jsonMapper.writeValueAsString(message)).get(6, TimeUnit.SECONDS);
			event.setPublishedAt(Instant.now());
			event.setAttempts(0);
			log.info("Checkout event published eventId={} checkoutId={}", event.getId(), message.getCheckoutId());
			return true;
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			defer(event);
			return false;
		}
		catch (Exception exception) {
			defer(event);
			return false;
		}
	}

	private void defer(RentalOutboxEvent event) {
		event.setAttempts(Math.min(event.getAttempts() + 1, 30));
		event.setNextAttemptAt(Instant.now().plusSeconds(Math.min(60, 1L << Math.min(6, event.getAttempts()))));
		log.warn("Checkout event delivery deferred eventId={} attempt={}", event.getId(), event.getAttempts());
	}

	@Transactional
	public void requeue(UUID id) {
		outboxRepository.findForUpdate(id).ifPresent(event -> {
			event.setPublishedAt(null);
			event.setNextAttemptAt(Instant.now());
		});
	}

}
