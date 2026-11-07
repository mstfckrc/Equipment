package com.equipmenthub.authservice.service;

import com.equipmenthub.authservice.entity.AuthRefreshSession;
import com.equipmenthub.authservice.entity.AuthRole;
import com.equipmenthub.authservice.entity.AuthUser;
import com.equipmenthub.authservice.repository.AuthRefreshSessionRepository;
import com.equipmenthub.authservice.repository.AuthUserRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenServiceTest {

	@Test
	void replayedRefreshTokenRevokesItsWholeFamily() {
		AuthRefreshSessionRepository sessions = mock(AuthRefreshSessionRepository.class);
		AuthUserRepository users = mock(AuthUserRepository.class);
		RefreshTokenService service = new RefreshTokenService(sessions, users);
		UUID familyId = UUID.randomUUID();
		AuthUser user = new AuthUser("customer@example.com", "hash", AuthRole.CUSTOMER);
		AuthRefreshSession replayedSession = new AuthRefreshSession(
				user, familyId, "stored-hash", Instant.now().minusSeconds(60), Instant.now().plusSeconds(600));
		replayedSession.setUsedAt(Instant.now().minusSeconds(30));
		when(sessions.findByTokenHashForUpdate(any(String.class))).thenReturn(Optional.of(replayedSession));

		assertTrue(service.rotate("replayed-token").isEmpty());
		verify(sessions).revokeFamily(eq(familyId), any(Instant.class));
	}

}
