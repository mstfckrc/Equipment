package com.equipmenthub.authservice.repository;

import com.equipmenthub.authservice.entity.StoreRegistration;
import com.equipmenthub.authservice.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRegistrationRepository extends JpaRepository<StoreRegistration, UUID> {

	@EntityGraph(attributePaths = "user")
	List<StoreRegistration> findAllByStoreIdIsNotNullOrderBySubmittedAtDesc();

	@EntityGraph(attributePaths = "user")
	Optional<StoreRegistration> findByIdAndStoreIdIsNotNull(UUID id);

	Optional<StoreRegistration> findByUserEmailIgnoreCase(String email);

	@Query("select registration.storeId from StoreRegistration registration "
			+ "where registration.storeId is not null "
			+ "and (registration.user.accountStatus = :status or registration.user.accountStatus is null)")
	List<UUID> findStoreIdsByAccountStatus(@Param("status") AccountStatus status);

}
