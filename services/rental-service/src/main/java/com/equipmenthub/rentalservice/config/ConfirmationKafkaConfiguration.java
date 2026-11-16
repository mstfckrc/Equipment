package com.equipmenthub.rentalservice.config;

import com.equipmenthub.rentalservice.messaging.CheckoutConfirmedEvent;
import com.equipmenthub.rentalservice.messaging.CheckoutEventCodec;
import com.equipmenthub.rentalservice.messaging.RentalKafkaTopics;
import com.equipmenthub.rentalservice.repository.RentalOutboxEventRepository;
import com.equipmenthub.rentalservice.service.ConfirmationPersistenceService;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.util.backoff.FixedBackOff;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
public class ConfirmationKafkaConfiguration {

	@Bean
	NewTopic checkoutConfirmedTopic() {
		return topic(RentalKafkaTopics.CHECKOUT_CONFIRMED, 7);
	}

	@Bean
	NewTopic confirmationDeadLetterTopic() {
		return topic(RentalKafkaTopics.CONFIRMATION_DLT, 14);
	}

	private NewTopic topic(String name, int retentionDays) {
		return TopicBuilder.name(name).partitions(3).replicas(1)
				.config("retention.ms", Long.toString(retentionDays * 86400000L))
				.config("retention.bytes", "268435456")
				.config("segment.bytes", "67108864").config("segment.ms", "3600000").build();
	}

	@Bean
	DefaultErrorHandler confirmationErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate,
			CheckoutEventCodec eventCodec, ConfirmationPersistenceService persistenceService,
			RentalOutboxEventRepository outboxRepository) {
		DefaultErrorHandler handler = new DefaultErrorHandler((record, exception) -> {
			try {
				// Only the original small message is retained; no raw exception/stack trace headers.
				kafkaTemplate.send(RentalKafkaTopics.CONFIRMATION_DLT, record.key(), record.value())
						.get(6, TimeUnit.SECONDS);
			}
			catch (InterruptedException interrupted) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException("Dead letter publication interrupted");
			}
			catch (Exception sendFailure) {
				throw new IllegalStateException("Dead letter publication unavailable");
			}
			CheckoutConfirmedEvent event;
			try {
				event = eventCodec.read(record.value() instanceof String text ? text : null);
			}
			catch (IllegalArgumentException invalidEvent) {
				return;
			}
			if (event.getCheckoutId().toString().equals(record.key())
					&& outboxRepository.existsByIdAndCheckout_Id(event.getEventId(), event.getCheckoutId())) {
				persistenceService.markFailed(event.getCheckoutId());
			}
		}, new FixedBackOff(2000, 3));
		handler.setBackOffFunction((record, exception) -> infrastructureFailure(exception)
				? new FixedBackOff(5000, FixedBackOff.UNLIMITED_ATTEMPTS) : new FixedBackOff(2000, 3));
		return handler;
	}

	private boolean infrastructureFailure(Throwable exception) {
		for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
			if (cause instanceof TransientDataAccessException || cause instanceof DataAccessResourceFailureException
					|| cause instanceof CannotCreateTransactionException
					|| cause instanceof org.hibernate.exception.JDBCConnectionException) {
				return true;
			}
		}
		return false;
	}

}
