package com.equipmenthub.authservice.config;

import com.equipmenthub.authservice.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshCookieFactory {

	private static final String COOKIE_PATH = "/auth";
	private final boolean secure;

	public RefreshCookieFactory(@Value("${auth.refresh-cookie.secure:false}") boolean secure) {
		this.secure = secure;
	}

	public ResponseCookie create(String token, Duration maxAge) {
		return ResponseCookie.from(RefreshTokenService.COOKIE_NAME, token)
				.httpOnly(true)
				.secure(secure)
				.path(COOKIE_PATH)
				.sameSite("Lax")
				.maxAge(maxAge)
				.build();
	}

	public ResponseCookie clear() {
		return ResponseCookie.from(RefreshTokenService.COOKIE_NAME, "")
				.httpOnly(true)
				.secure(secure)
				.path(COOKIE_PATH)
				.sameSite("Lax")
				.maxAge(Duration.ZERO)
				.build();
	}

}
