package com.equipmenthub.rentalservice.repository;

import com.equipmenthub.rentalservice.entity.RentalInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RentalInventoryRepository extends JpaRepository<RentalInventory, UUID> {

	List<RentalInventory> findAllByStoreIdOrderByCreatedAtDesc(UUID storeId);

	Optional<RentalInventory> findByStoreIdAndListingId(UUID storeId, UUID listingId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select inventory from RentalInventory inventory "
			+ "where inventory.storeId = :storeId and inventory.listingId = :listingId")
	Optional<RentalInventory> findForUpdate(
			@Param("storeId") UUID storeId, @Param("listingId") UUID listingId);

}
