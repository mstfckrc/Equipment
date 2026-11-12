package com.equipmenthub.rentalservice.repository;

import com.equipmenthub.rentalservice.entity.RentalCheckout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RentalCheckoutRepository extends JpaRepository<RentalCheckout, UUID> {

	Optional<RentalCheckout> findByCustomerIdAndIdempotencyKey(UUID customerId, String idempotencyKey);

}
