package com.equipmenthub.rentalservice.repository;

import com.equipmenthub.rentalservice.entity.EquipmentUnit;
import com.equipmenthub.rentalservice.entity.EquipmentUnitStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EquipmentUnitRepository extends JpaRepository<EquipmentUnit, UUID> {

	long countByInventory_IdAndStatusNot(UUID inventoryId, EquipmentUnitStatus status);

	long countByInventory_IdAndStatus(UUID inventoryId, EquipmentUnitStatus status);

	List<EquipmentUnit> findAllByInventory_IdOrderByUnitNumberAsc(UUID inventoryId);

	@Query("select unit.inventory.id as inventoryId, unit.status as status, count(unit.id) as unitCount "
			+ "from EquipmentUnit unit where unit.inventory.id in :inventoryIds "
			+ "group by unit.inventory.id, unit.status")
	List<EquipmentUnitCount> countByInventoryIds(@Param("inventoryIds") Collection<UUID> inventoryIds);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select unit from EquipmentUnit unit where unit.inventory.id = :inventoryId "
			+ "and unit.status = :status order by unit.unitNumber desc")
	List<EquipmentUnit> findForRetirement(
			@Param("inventoryId") UUID inventoryId, @Param("status") EquipmentUnitStatus status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select unit from EquipmentUnit unit where unit.id = :unitId "
			+ "and unit.inventory.storeId = :storeId")
	Optional<EquipmentUnit> findForUpdate(
			@Param("unitId") UUID unitId, @Param("storeId") UUID storeId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select unit from EquipmentUnit unit where unit.inventory.id = :inventoryId "
			+ "and unit.status = :status order by unit.unitNumber asc")
	List<EquipmentUnit> findReadyForHandover(
			@Param("inventoryId") UUID inventoryId, @Param("status") EquipmentUnitStatus status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select unit from EquipmentUnit unit where unit.inventory.id = :inventoryId "
			+ "and unit.id in :unitIds and unit.status = :status order by unit.unitNumber asc")
	List<EquipmentUnit> findSelectedReadyForHandover(
			@Param("inventoryId") UUID inventoryId,
			@Param("unitIds") Collection<UUID> unitIds,
			@Param("status") EquipmentUnitStatus status);

	@Query("select unit from EquipmentUnit unit join fetch unit.inventory inventory where unit.id = :unitId")
	Optional<EquipmentUnit> findWithInventory(@Param("unitId") UUID unitId);

}
