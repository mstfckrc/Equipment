package com.equipmenthub.rentalservice.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class CheckoutEventCodec {

	@Autowired
	private JsonMapper jsonMapper;

	public CheckoutConfirmedEvent read(String value) {
		if (value == null || value.length() > 4096) {
			throw new IllegalArgumentException("Invalid checkout event size");
		}
		CheckoutConfirmedEvent event;
		try {
			event = jsonMapper.readValue(value, CheckoutConfirmedEvent.class);
		}
		catch (RuntimeException exception) {
			throw new IllegalArgumentException("Invalid checkout event JSON");
		}
		if (event == null || event.getEventId() == null || event.getCheckoutId() == null
				|| event.getOccurredAt() == null || event.getSchemaVersion() != 1
				|| !RentalKafkaTopics.EVENT_TYPE.equals(event.getEventType())) {
			throw new IllegalArgumentException("Invalid checkout event contract");
		}
		return event;
	}

}
