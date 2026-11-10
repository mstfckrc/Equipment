package com.equipmenthub.authservice.service;

import com.equipmenthub.authservice.dto.request.LoginRequest;
import com.equipmenthub.authservice.dto.request.RegisterRequest;
import com.equipmenthub.authservice.dto.request.RegistrationType;
import com.equipmenthub.authservice.dto.request.StoreRegistrationReviewRequest;
import com.equipmenthub.authservice.dto.response.IssuedAuthSession;
import com.equipmenthub.authservice.dto.response.LoginResponse;
import com.equipmenthub.authservice.dto.response.RegisterResponse;
import com.equipmenthub.authservice.dto.response.RotatedRefreshToken;
import com.equipmenthub.authservice.dto.response.StoreRegistrationResponse;
import com.equipmenthub.authservice.entity.AccountStatus;
import com.equipmenthub.authservice.entity.AuthRole;
import com.equipmenthub.authservice.entity.AuthUser;
import com.equipmenthub.authservice.entity.StoreRegistration;
import com.equipmenthub.authservice.exception.AuthException;
import com.equipmenthub.authservice.repository.AuthUserRepository;
import com.equipmenthub.authservice.repository.StoreRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

	private static final String TOKEN_TYPE = "Bearer";

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final AuthUserRepository authUserRepository;
	private final StoreRegistrationRepository storeRegistrationRepository;
	private final PasswordEncoder passwordEncoder;

	public IssuedAuthSession login(LoginRequest request) {
		Authentication authentication;
		try {
			authentication = authenticationManager.authenticate(
					UsernamePasswordAuthenticationToken.unauthenticated(
							request.getEmail().trim(), request.getPassword()));
		}
		catch (InternalAuthenticationServiceException exception) {
			throw exception;
		}
		catch (AuthenticationException exception) {
			throw new AuthException(AuthException.Kind.UNAUTHORIZED, "Invalid email or password");
		}

		AuthUser user = authUserRepository.findById(UUID.fromString(authentication.getName()))
				.orElseThrow(() -> new AuthException(
						AuthException.Kind.UNAUTHORIZED, "Invalid email or password"));
		if (user.getAccountStatus() == AccountStatus.PENDING) {
			throw new AuthException(AuthException.Kind.FORBIDDEN, "Store account is awaiting review");
		}
		if (user.getAccountStatus() == AccountStatus.REJECTED) {
			throw new AuthException(
					AuthException.Kind.FORBIDDEN, "Store account registration was rejected");
		}

		LoginResponse loginResponse = createLoginResponse(jwtService.createAccessToken(authentication));
		RefreshTokenService.CreatedRefreshToken refreshToken = refreshTokenService
				.create(UUID.fromString(authentication.getName()));
		return new IssuedAuthSession(loginResponse, refreshToken.getToken(), refreshToken.getExpiresAt());
	}

	@Transactional
	public RegisterResponse register(RegisterRequest request) {

		String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

		if (authUserRepository.existsByEmailIgnoreCase(email)) {
			throw new AuthException(AuthException.Kind.CONFLICT, "Registration could not be completed");
		}

		if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new AuthException(AuthException.Kind.BAD_REQUEST, "Invalid request");
		}
		validateRegistrationDetails(request);

		AuthRole role = switch (request.getAccountType()) {
			case CUSTOMER -> AuthRole.CUSTOMER;
			case STORE_OWNER -> AuthRole.STORE_OWNER;
		};

		AuthUser user = new AuthUser(email, passwordEncoder.encode(request.getPassword()), role,
				normalizeOptional(request.getFirstName()), normalizeOptional(request.getLastName()));
		user.setAccountStatus(request.getAccountType() == RegistrationType.STORE_OWNER
				? AccountStatus.PENDING : AccountStatus.ACTIVE);
		user = authUserRepository.save(user);
		UUID registrationId = null;

		if (request.getAccountType() == RegistrationType.STORE_OWNER) {
			StoreRegistration registration = storeRegistrationRepository.save(
					new StoreRegistration(user, request.getStoreName(), request.getPhone(), request.getCity(),
							request.getDistrict(), request.getAddressLine()));
			registrationId = registration.getId();
		}

		return RegisterResponse.builder()
				.id(user.getId())
				.registrationId(registrationId)
				.email(user.getEmail())
				.accountType(request.getAccountType())
				.status(user.getAccountStatus())
				.build();
	}

	@Transactional(readOnly = true)
	public List<StoreRegistrationResponse> getPendingStoreRegistrations() {
		return storeRegistrationRepository.findPendingRegistrations(AccountStatus.PENDING).stream()
				.map(this::toStoreRegistrationResponse)
				.toList();
	}

	@Transactional
	public StoreRegistrationResponse approveStoreRegistration(UUID registrationId, UUID adminId) {
		StoreRegistration registration = findPendingRegistration(registrationId);
		registration.getUser().setAccountStatus(AccountStatus.ACTIVE);
		registration.setReviewedAt(Instant.now());
		registration.setReviewedBy(adminId);
		registration.setRejectionReason(null);
		return toStoreRegistrationResponse(registration);
	}

	@Transactional
	public StoreRegistrationResponse rejectStoreRegistration(
			UUID registrationId, UUID adminId, StoreRegistrationReviewRequest request) {
		StoreRegistration registration = findPendingRegistration(registrationId);
		registration.getUser().setAccountStatus(AccountStatus.REJECTED);
		registration.setReviewedAt(Instant.now());
		registration.setReviewedBy(adminId);
		registration.setRejectionReason(normalizeOptional(request.getReason()));
		return toStoreRegistrationResponse(registration);
	}

	public IssuedAuthSession refresh(String refreshToken) {
		RotatedRefreshToken rotated = refreshTokenService.rotate(refreshToken)
				.orElseThrow(() -> new AuthException(
						AuthException.Kind.UNAUTHORIZED, "Refresh session is invalid or expired"));
		LoginResponse loginResponse = createLoginResponse(
				jwtService.createAccessToken(rotated.getUserId(), rotated.getRole()));
		return new IssuedAuthSession(loginResponse, rotated.getToken(), rotated.getExpiresAt());
	}

	public void logout(String refreshToken) {
		refreshTokenService.revoke(refreshToken);
	}

	private LoginResponse createLoginResponse(String accessToken) {
		return LoginResponse.builder()
				.accessToken(accessToken)
				.tokenType(TOKEN_TYPE)
				.expiresInSeconds(jwtService.getAccessTokenLifetimeSeconds())
				.build();
	}

	private void validateRegistrationDetails(RegisterRequest request) {
		if (request.getAccountType() == RegistrationType.CUSTOMER) {
			if (isBlank(request.getFirstName()) || isBlank(request.getLastName())) {
				throw new AuthException(AuthException.Kind.BAD_REQUEST, "Invalid request");
			}
			return;
		}
		if (isBlank(request.getStoreName()) || isBlank(request.getPhone()) || isBlank(request.getCity())
				|| isBlank(request.getDistrict()) || isBlank(request.getAddressLine())) {
			throw new AuthException(AuthException.Kind.BAD_REQUEST, "Invalid request");
		}
	}

	private StoreRegistration findPendingRegistration(UUID registrationId) {
		StoreRegistration registration = storeRegistrationRepository.findById(registrationId)
				.orElseThrow(() -> new AuthException(
						AuthException.Kind.NOT_FOUND, "Store registration was not found"));
		if (registration.getUser().getAccountStatus() != AccountStatus.PENDING) {
			throw new AuthException(AuthException.Kind.CONFLICT, "Store registration is no longer pending");
		}
		return registration;
	}

	private StoreRegistrationResponse toStoreRegistrationResponse(StoreRegistration registration) {
		AuthUser user = registration.getUser();
		return StoreRegistrationResponse.builder()
				.id(registration.getId())
				.accountId(user.getId())
				.email(user.getEmail())
				.storeName(registration.getStoreName())
				.phone(registration.getPhone())
				.city(registration.getCity())
				.district(registration.getDistrict())
				.addressLine(registration.getAddressLine())
				.status(user.getAccountStatus())
				.submittedAt(registration.getSubmittedAt())
				.reviewedAt(registration.getReviewedAt())
				.reviewedBy(registration.getReviewedBy())
				.rejectionReason(registration.getRejectionReason())
				.build();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private String normalizeOptional(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

}
