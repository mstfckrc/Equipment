package com.equipmenthub.rentalservice.repository;

import com.equipmenthub.rentalservice.entity.ConfirmationStatus;

import java.util.UUID;

public interface ConfirmationSummary {

	UUID getCheckoutId();
	ConfirmationStatus getStatus();

}
