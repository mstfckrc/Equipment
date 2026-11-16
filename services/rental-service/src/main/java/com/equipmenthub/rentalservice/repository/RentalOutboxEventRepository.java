package com.equipmenthub.rentalservice.repository;

import com.equipmenthub.rentalservice.entity.RentalOutboxEvent;
import com.equipmenthub.rentalservice.entity.ConfirmationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RentalOutboxEventRepository extends JpaRepository<RentalOutboxEvent, UUID> {

	boolean existsByIdAndCheckout_Id(UUID id, UUID checkoutId);

	Optional<RentalOutboxEvent> findByCheckout_Id(UUID checkoutId);

	@Query("select e.id from RentalOutboxEvent e where e.publishedAt is null "
			+ "and e.nextAttemptAt <= :now order by e.occurredAt, e.id")
	List<UUID> findDueIds(@Param("now") Instant now, Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select e from RentalOutboxEvent e join fetch e.checkout where e.id = :id")
	Optional<RentalOutboxEvent> findForUpdate(@Param("id") UUID id);

	@Query("select e.id from RentalOutboxEvent e, RentalConfirmation c "
			+ "where e.checkout = c.checkout and c.status = :status "
			+ "and e.publishedAt < :before order by e.publishedAt")
	List<UUID> findPublishedIds(@Param("status") ConfirmationStatus status,
			@Param("before") Instant before, Pageable pageable);

}
