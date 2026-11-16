package com.equipmenthub.rentalservice.messaging;

public final class RentalKafkaTopics {

	public static final String CHECKOUT_CONFIRMED = "equipmenthub.rental.checkout-confirmed.v1";
	public static final String CONFIRMATION_DLT = CHECKOUT_CONFIRMED + ".dlt";
	public static final String EVENT_TYPE = "RentalCheckoutConfirmed";

	private RentalKafkaTopics() {
	}

}
