package com.equipmenthub.authservice.service;

import com.equipmenthub.authservice.dto.response.RotatedRefreshToken;
import com.equipmenthub.authservice.entity.AuthRefreshSession;
import com.equipmenthub.authservice.entity.AuthUser;
import com.equipmenthub.authservice.repository.AuthRefreshSessionRepository;
import com.equipmenthub.authservice.repository.AuthUserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	public static final String COOKIE_NAME = "EQUIPMENTHUB_REFRESH";
	private static final Duration SESSION_LIFETIME = Duration.ofDays(7);
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private final AuthRefreshSessionRepository refreshSessionRepository;
	private final AuthUserRepository authUserRepository;

	@Transactional
	public CreatedRefreshToken create(UUID userId) {
		AuthUser user = authUserRepository.findById(userId)
				.orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
		Instant now = Instant.now();
		Instant expiresAt = now.plus(SESSION_LIFETIME);
		UUID familyId = UUID.randomUUID();
		String rawToken = generateToken();
		refreshSessionRepository.save(new AuthRefreshSession(
				user, familyId, hashToken(rawToken), now, expiresAt));
		return new CreatedRefreshToken(rawToken, expiresAt);
	}

	@Transactional
	public Optional<RotatedRefreshToken> rotate(String rawToken) {
		if (rawToken == null || rawToken.isBlank()) {
			return Optional.empty();
		}

		Optional<AuthRefreshSession> found = refreshSessionRepository.findByTokenHashForUpdate(hashToken(rawToken));
		if (found.isEmpty()) {
			return Optional.empty();
		}

		AuthRefreshSession current = found.get();
		Instant now = Instant.now();
		if (current.getUsedAt() != null) {
			refreshSessionRepository.revokeFamily(current.getFamilyId(), now);
			return Optional.empty();
		}
		if (current.getRevokedAt() != null || !current.getExpiresAt().isAfter(now)) {
			refreshSessionRepository.revokeFamily(current.getFamilyId(), now);
			return Optional.empty();
		}

		UUID userId = current.getUser().getId();
		Optional<AuthUser> foundUser = authUserRepository.findById(userId);
		if (foundUser.isEmpty() || !foundUser.get().isEnabled()) {
			refreshSessionRepository.revokeFamily(current.getFamilyId(), now);
			return Optional.empty();
		}

		String nextRawToken = generateToken();
		current.setUsedAt(now);
		refreshSessionRepository.save(current);
		refreshSessionRepository.save(new AuthRefreshSession(
				foundUser.get(), current.getFamilyId(), hashToken(nextRawToken), now, current.getExpiresAt()));

		return Optional.of(new RotatedRefreshToken(
				userId, foundUser.get().getRole(), nextRawToken, current.getExpiresAt()));
	}

	@Transactional
	public void revoke(String rawToken) {
		if (rawToken == null || rawToken.isBlank()) {
			return;
		}

		refreshSessionRepository.findByTokenHashForUpdate(hashToken(rawToken))
				.ifPresent(session -> refreshSessionRepository.revokeFamily(session.getFamilyId(), Instant.now()));
	}

	private String generateToken() {
		byte[] randomBytes = new byte[32];
		SECURE_RANDOM.nextBytes(randomBytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
	}

	private String hashToken(String rawToken) {
		try {
			byte[] hash = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		}
		catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is unavailable", exception);
		}
	}

	@Getter
	public static class CreatedRefreshToken {

		private final String token;
		private final Instant expiresAt;

		private CreatedRefreshToken(String token, Instant expiresAt) {
			this.token = token;
			this.expiresAt = expiresAt;
		}

	}

}
