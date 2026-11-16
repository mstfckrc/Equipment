package com.equipmenthub.rentalservice.repository;

import com.equipmenthub.rentalservice.entity.RentalCheckout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RentalCheckoutRepository extends JpaRepository<RentalCheckout, UUID> {

	Optional<RentalCheckout> findByCustomerIdAndIdempotencyKey(UUID customerId, String idempotencyKey);

	Optional<RentalCheckout> findByIdAndCustomerId(UUID id, UUID customerId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from RentalCheckout c where c.id = :id")
	Optional<RentalCheckout> findForUpdate(@Param("id") UUID id);

	@Query("select c.id from RentalCheckout c where not exists "
			+ "(select d.id from RentalConfirmation d where d.checkout = c) order by c.createdAt, c.id")
	List<UUID> findWithoutConfirmation(Pageable pageable);

}
