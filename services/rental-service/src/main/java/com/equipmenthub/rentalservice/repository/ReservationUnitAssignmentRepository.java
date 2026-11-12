package com.equipmenthub.rentalservice.repository;

import com.equipmenthub.rentalservice.entity.ReservationUnitAssignment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ReservationUnitAssignmentRepository extends JpaRepository<ReservationUnitAssignment, UUID> {

	@Query("select assignment from ReservationUnitAssignment assignment "
			+ "join fetch assignment.equipmentUnit unit "
			+ "where assignment.reservationItem.id in :itemIds "
			+ "order by assignment.handedOverAt asc, unit.unitNumber asc")
	List<ReservationUnitAssignment> findAllByReservationItemIds(@Param("itemIds") Collection<UUID> itemIds);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select assignment from ReservationUnitAssignment assignment "
			+ "join fetch assignment.equipmentUnit unit "
			+ "where assignment.reservationItem.id = :itemId order by unit.unitNumber asc")
	List<ReservationUnitAssignment> findForUpdateByReservationItemId(@Param("itemId") UUID itemId);

}
