package com.equipmenthub.rentalservice.repository;

import com.equipmenthub.rentalservice.entity.ReservationItem;
import com.equipmenthub.rentalservice.entity.ReservationItemStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ReservationItemRepository extends JpaRepository<ReservationItem, UUID> {

	List<ReservationItem> findAllByReservation_IdOrderByPickupDateAscIdAsc(UUID reservationId);

	List<ReservationItem> findAllByReservation_IdInOrderByPickupDateAscIdAsc(Collection<UUID> reservationIds);

	@Query("select item from ReservationItem item join fetch item.reservation reservation "
			+ "where item.id = :itemId")
	Optional<ReservationItem> findWithReservation(@Param("itemId") UUID itemId);

	@Query("select item.pickupDate as pickupDate, item.returnDate as returnDate, item.quantity as quantity "
			+ "from ReservationItem item where item.listingId = :listingId and item.status = :status "
			+ "and item.pickupDate < :toDateExclusive and item.returnDate > :fromDate")
	List<ReservationInterval> findOverlapping(
			@Param("listingId") UUID listingId,
			@Param("status") ReservationItemStatus status,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDateExclusive") LocalDate toDateExclusive);

	@Query("select item.pickupDate as pickupDate, item.returnDate as returnDate, item.quantity as quantity "
			+ "from ReservationItem item where item.listingId = :listingId "
			+ "and item.status = :status and item.returnDate > :afterDate")
	List<ReservationInterval> findActiveAfter(
			@Param("listingId") UUID listingId,
			@Param("status") ReservationItemStatus status,
			@Param("afterDate") LocalDate afterDate);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select item from ReservationItem item join fetch item.reservation reservation "
			+ "where item.id = :itemId and reservation.storeId = :storeId")
	Optional<ReservationItem> findForStoreUpdate(
			@Param("itemId") UUID itemId, @Param("storeId") UUID storeId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select item from ReservationItem item join fetch item.reservation reservation "
			+ "join fetch reservation.checkout checkout "
			+ "where item.id = :itemId and checkout.customerId = :customerId")
	Optional<ReservationItem> findForCustomerUpdate(
			@Param("itemId") UUID itemId, @Param("customerId") UUID customerId);

}
