package com.equipmenthub.authservice.controller;

import com.equipmenthub.authservice.config.RefreshCookieFactory;
import com.equipmenthub.authservice.dto.request.LoginRequest;
import com.equipmenthub.authservice.dto.request.AccountEnabledUpdateRequest;
import com.equipmenthub.authservice.dto.request.RegisterRequest;
import com.equipmenthub.authservice.dto.request.StoreRegistrationReviewRequest;
import com.equipmenthub.authservice.dto.response.CsrfTokenResponse;
import com.equipmenthub.authservice.dto.response.AccountSummaryResponse;
import com.equipmenthub.authservice.dto.response.IssuedAuthSession;
import com.equipmenthub.authservice.dto.response.LoginResponse;
import com.equipmenthub.authservice.dto.response.RegisterResponse;
import com.equipmenthub.authservice.dto.response.StoreRegistrationResponse;
import com.equipmenthub.authservice.dto.response.StoreRegistrationSummaryResponse;
import com.equipmenthub.authservice.service.AuthService;
import com.equipmenthub.authservice.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
public class AuthController {

	@Autowired
	private AuthService authService;

	@Autowired
	private RefreshCookieFactory refreshCookieFactory;

	@GetMapping("/csrf")
	public CsrfTokenResponse csrf(CsrfToken csrfToken) {
		return new CsrfTokenResponse(csrfToken.getToken(), csrfToken.getHeaderName());
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	@GetMapping("/admin/store-registrations")
	public List<StoreRegistrationSummaryResponse> storeRegistrations() {
		return authService.getStoreRegistrations();
	}

	@GetMapping("/admin/accounts")
	public List<AccountSummaryResponse> accounts() {
		return authService.getAccounts();
	}

	@PatchMapping("/admin/accounts/{accountId}")
	public AccountSummaryResponse updateAccountEnabled(
			@PathVariable UUID accountId, @Valid @RequestBody AccountEnabledUpdateRequest request) {
		return authService.updateAccountEnabled(accountId, request);
	}

	@GetMapping("/internal/approved-store-ids")
	public List<UUID> approvedStoreIds() {
		return authService.getApprovedStoreIds();
	}

	@GetMapping("/admin/store-registrations/{registrationId}")
	public StoreRegistrationResponse storeRegistration(@PathVariable UUID registrationId) {
		return authService.getStoreRegistration(registrationId);
	}

	@PatchMapping("/admin/store-registrations/{registrationId}")
	public StoreRegistrationResponse reviewStoreRegistration(
			@PathVariable UUID registrationId,
			@Valid @RequestBody StoreRegistrationReviewRequest request,
			Authentication authentication) {
		return authService.reviewStoreRegistration(
				registrationId, UUID.fromString(authentication.getName()), request);
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(
			@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
		return issueSession(authService.login(request), HttpStatus.OK, response);
	}

	@PostMapping("/refresh")
	public ResponseEntity<LoginResponse> refresh(
			@CookieValue(name = RefreshTokenService.COOKIE_NAME, required = false) String refreshToken,
			HttpServletResponse response) {
		return issueSession(authService.refresh(refreshToken), HttpStatus.OK, response);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(
			@CookieValue(name = RefreshTokenService.COOKIE_NAME, required = false) String refreshToken,
			HttpServletResponse response) {
		authService.logout(refreshToken);
		response.addHeader(HttpHeaders.SET_COOKIE, refreshCookieFactory.clear().toString());
		return ResponseEntity.noContent().build();
	}

	private ResponseEntity<LoginResponse> issueSession(
			IssuedAuthSession session, HttpStatus status, HttpServletResponse response) {
		Duration remaining = Duration.between(Instant.now(), session.getRefreshExpiresAt());
		response.addHeader(HttpHeaders.SET_COOKIE,
				refreshCookieFactory.create(session.getRefreshToken(), remaining).toString());
		return ResponseEntity.status(status).body(session.getLoginResponse());
	}
}
