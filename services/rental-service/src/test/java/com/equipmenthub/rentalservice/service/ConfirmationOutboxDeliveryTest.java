package com.equipmenthub.rentalservice.service;

import com.equipmenthub.rentalservice.entity.RentalCheckout;
import com.equipmenthub.rentalservice.entity.RentalOutboxEvent;
import com.equipmenthub.rentalservice.messaging.RentalKafkaTopics;
import com.equipmenthub.rentalservice.repository.RentalOutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmationOutboxDeliveryTest {

	@Mock private RentalOutboxEventRepository outboxRepository;
	@Mock private KafkaTemplate<Object, Object> kafkaTemplate;
	@InjectMocks private RentalOutboxDeliveryService service;
	private RentalOutboxEvent event;

	@BeforeEach
	void sourceEvent() {
		ReflectionTestUtils.setField(service, "jsonMapper", JsonMapper.builder().build());
		RentalCheckout checkout = new RentalCheckout();
		checkout.setId(UUID.randomUUID());
		event = new RentalOutboxEvent();
		event.setId(UUID.randomUUID());
		event.setCheckout(checkout);
		event.setOccurredAt(Instant.now());
		event.setNextAttemptAt(Instant.EPOCH);
		when(outboxRepository.findForUpdate(event.getId())).thenReturn(Optional.of(event));
	}

	@Test
	void onlyMarksPublishedAfterBrokerAcknowledgement() {
		when(kafkaTemplate.send(eq(RentalKafkaTopics.CHECKOUT_CONFIRMED), any(), any()))
				.thenReturn(CompletableFuture.completedFuture(null));
		assertThat(service.deliver(event.getId())).isTrue();
		assertThat(event.getPublishedAt()).isNotNull();
	}

	@Test
	void failedDeliveryRemainsPendingWithBackoff() {
		when(kafkaTemplate.send(eq(RentalKafkaTopics.CHECKOUT_CONFIRMED), any(), any()))
				.thenReturn(CompletableFuture.failedFuture(new IllegalStateException("Unavailable")));
		assertThat(service.deliver(event.getId())).isFalse();
		assertThat(event.getPublishedAt()).isNull();
		assertThat(event.getAttempts()).isEqualTo(1);
		assertThat(event.getNextAttemptAt()).isAfter(Instant.now());
	}

}
