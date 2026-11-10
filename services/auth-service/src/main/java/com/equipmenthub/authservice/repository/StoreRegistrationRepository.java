package com.equipmenthub.authservice.repository;

import com.equipmenthub.authservice.entity.AccountStatus;
import com.equipmenthub.authservice.entity.StoreRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StoreRegistrationRepository extends JpaRepository<StoreRegistration, UUID> {

	@Query("select registration from StoreRegistration registration "
			+ "where registration.user.accountStatus = :pending order by registration.submittedAt")
	List<StoreRegistration> findPendingRegistrations(@Param("pending") AccountStatus pending);

}
