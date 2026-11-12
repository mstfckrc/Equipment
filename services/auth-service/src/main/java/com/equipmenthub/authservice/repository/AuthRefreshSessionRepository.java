package com.equipmenthub.authservice.repository;

import com.equipmenthub.authservice.entity.AuthRefreshSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AuthRefreshSessionRepository extends JpaRepository<AuthRefreshSession, UUID> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select session from AuthRefreshSession session where session.tokenHash = :tokenHash")
	Optional<AuthRefreshSession> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

	@Modifying
	@Query("update AuthRefreshSession session set session.revokedAt = :revokedAt "
			+ "where session.familyId = :familyId and session.revokedAt is null")
	int revokeFamily(@Param("familyId") UUID familyId, @Param("revokedAt") Instant revokedAt);

	@Modifying
	@Query("update AuthRefreshSession session set session.revokedAt = :revokedAt "
			+ "where session.user.id = :userId and session.revokedAt is null")
	int revokeAllForUser(@Param("userId") UUID userId, @Param("revokedAt") Instant revokedAt);

}
