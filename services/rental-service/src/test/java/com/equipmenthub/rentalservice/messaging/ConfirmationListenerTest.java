package com.equipmenthub.rentalservice.messaging;

import com.equipmenthub.rentalservice.repository.RentalOutboxEventRepository;
import com.equipmenthub.rentalservice.service.ConfirmationPersistenceService;
import com.equipmenthub.rentalservice.service.ConfirmationPreparationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmationListenerTest {

	@Mock private ConfirmationPersistenceService persistenceService;
	@Mock private ConfirmationPreparationService preparationService;
	@Mock private RentalOutboxEventRepository outboxRepository;
	@InjectMocks private ConfirmationListener listener;
	private final JsonMapper mapper = JsonMapper.builder().build();

	@BeforeEach
	void setMapper() {
		CheckoutEventCodec codec = new CheckoutEventCodec();
		ReflectionTestUtils.setField(codec, "jsonMapper", mapper);
		ReflectionTestUtils.setField(listener, "eventCodec", codec);
	}

	@Test
	void consumesARecordedCheckoutJob() {
		CheckoutConfirmedEvent event = event();
		when(outboxRepository.existsByIdAndCheckout_Id(event.getEventId(), event.getCheckoutId())).thenReturn(true);
		listener.onCheckout(record(event));
		verify(preparationService).prepare(event.getCheckoutId());
	}

	@Test
	void ignoresAReplayAfterTheDocumentIsReady() {
		CheckoutConfirmedEvent event = event();
		when(persistenceService.isReady(event.getCheckoutId())).thenReturn(true);
		listener.onCheckout(record(event));
		verifyNoInteractions(preparationService, outboxRepository);
	}

	@Test
	void refusesUnknownSchemaAndWrongPartitionKey() {
		CheckoutConfirmedEvent event = event();
		event.setSchemaVersion(2);
		assertThatThrownBy(() -> listener.onCheckout(record(event))).isInstanceOf(IllegalArgumentException.class);
		event.setSchemaVersion(1);
		assertThatThrownBy(() -> listener.onCheckout(new ConsumerRecord<>(RentalKafkaTopics.CHECKOUT_CONFIRMED,
				0, 0, "another-checkout", mapper.writeValueAsString(event))))
				.isInstanceOf(IllegalArgumentException.class);
		verifyNoInteractions(preparationService);
	}

	private CheckoutConfirmedEvent event() {
		return new CheckoutConfirmedEvent(UUID.randomUUID(), RentalKafkaTopics.EVENT_TYPE, 1,
				UUID.randomUUID(), Instant.now());
	}

	private ConsumerRecord<String, String> record(CheckoutConfirmedEvent event) {
		return new ConsumerRecord<>(RentalKafkaTopics.CHECKOUT_CONFIRMED, 0, 0,
				event.getCheckoutId().toString(), mapper.writeValueAsString(event));
	}

}
