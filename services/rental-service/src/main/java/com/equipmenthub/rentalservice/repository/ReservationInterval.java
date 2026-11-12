package com.equipmenthub.rentalservice.repository;

import java.time.LocalDate;

public interface ReservationInterval {

	LocalDate getPickupDate();

	LocalDate getReturnDate();

	int getQuantity();

}
