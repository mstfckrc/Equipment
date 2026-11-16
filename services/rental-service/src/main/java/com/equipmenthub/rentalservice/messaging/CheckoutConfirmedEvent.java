package com.equipmenthub.rentalservice.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutConfirmedEvent {

	private UUID eventId;
	private String eventType;
	private int schemaVersion;
	private UUID checkoutId;
	private Instant occurredAt;

}
