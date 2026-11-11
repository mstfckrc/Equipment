package com.equipmenthub.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class IssuedAuthSession {

	private final LoginResponse loginResponse;
	private final String refreshToken;
	private final Instant refreshExpiresAt;

}
