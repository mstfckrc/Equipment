package com.equipmenthub.rentalservice.repository;

import com.equipmenthub.rentalservice.entity.RentalConfirmation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RentalConfirmationRepository extends JpaRepository<RentalConfirmation, UUID> {

	Optional<RentalConfirmation> findByCheckout_Id(UUID checkoutId);

	boolean existsByCheckout_Id(UUID checkoutId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from RentalConfirmation c where c.checkout.id = :checkoutId")
	Optional<RentalConfirmation> findForUpdate(@Param("checkoutId") UUID checkoutId);

	@Query("select c.checkout.id as checkoutId, c.status as status from RentalConfirmation c "
			+ "where c.checkout.id in :checkoutIds")
	List<ConfirmationSummary> findSummaries(@Param("checkoutIds") Collection<UUID> checkoutIds);

}
