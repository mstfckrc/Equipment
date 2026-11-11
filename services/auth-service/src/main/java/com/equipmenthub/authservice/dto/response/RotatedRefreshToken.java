package com.equipmenthub.authservice.dto.response;

import com.equipmenthub.authservice.entity.AuthRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class RotatedRefreshToken {

	private final UUID userId;
	private final AuthRole role;
	private final String token;
	private final Instant expiresAt;

}
