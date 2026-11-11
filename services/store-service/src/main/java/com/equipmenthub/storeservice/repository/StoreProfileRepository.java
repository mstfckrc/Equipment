package com.equipmenthub.storeservice.repository;

import com.equipmenthub.storeservice.entity.StoreProfile;
import com.equipmenthub.storeservice.entity.StoreStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreProfileRepository extends JpaRepository<StoreProfile, UUID> {

	Optional<StoreProfile> findByOwnerAccountId(UUID ownerAccountId);

	List<StoreProfile> findAllByIdInAndStatusOrderByStoreNameAsc(Collection<UUID> ids, StoreStatus status);

	Optional<StoreProfile> findByIdAndStatus(UUID id, StoreStatus status);

}
