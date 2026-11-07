package com.equipmenthub.authservice.dto.response;

import lombok.Getter;

import java.time.Instant;

@Getter
public class IssuedAuthSession {

	private final LoginResponse loginResponse;
	private final String refreshToken;
	private final Instant refreshExpiresAt;

	public IssuedAuthSession(LoginResponse loginResponse, String refreshToken, Instant refreshExpiresAt) {
		this.loginResponse = loginResponse;
		this.refreshToken = refreshToken;
		this.refreshExpiresAt = refreshExpiresAt;
	}

}
