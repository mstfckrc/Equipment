package com.equipmenthub.authservice.dto.response;

import com.equipmenthub.authservice.entity.AuthRole;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class RotatedRefreshToken {

	private final UUID userId;
	private final AuthRole role;
	private final String token;
	private final Instant expiresAt;

	public RotatedRefreshToken(UUID userId, AuthRole role, String token, Instant expiresAt) {
		this.userId = userId;
		this.role = role;
		this.token = token;
		this.expiresAt = expiresAt;
	}

}
