package com.equipmenthub.rentalservice.repository;

import com.equipmenthub.rentalservice.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

	List<Reservation> findAllByCheckout_CustomerIdOrderByCreatedAtDesc(UUID customerId);

	List<Reservation> findAllByStoreIdOrderByCreatedAtDesc(UUID storeId);

	List<Reservation> findAllByCheckout_IdOrderByCreatedAtAsc(UUID checkoutId);

	@Query("select reservation from Reservation reservation join fetch reservation.checkout checkout "
			+ "where checkout.id = :checkoutId order by reservation.createdAt asc")
	List<Reservation> findAllWithCheckoutByCheckoutId(@Param("checkoutId") UUID checkoutId);

	@Query("select reservation from Reservation reservation join fetch reservation.checkout checkout "
			+ "where checkout.customerId = :customerId order by reservation.createdAt desc")
	List<Reservation> findAllWithCheckoutByCustomerId(@Param("customerId") UUID customerId);

	@Query("select reservation from Reservation reservation join fetch reservation.checkout checkout "
			+ "where reservation.storeId = :storeId order by reservation.createdAt desc")
	List<Reservation> findAllWithCheckoutByStoreId(@Param("storeId") UUID storeId);

}
