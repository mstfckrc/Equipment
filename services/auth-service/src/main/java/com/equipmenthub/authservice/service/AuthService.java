package com.equipmenthub.authservice.service;

import com.equipmenthub.authservice.dto.request.LoginRequest;
import com.equipmenthub.authservice.dto.request.AccountEnabledUpdateRequest;
import com.equipmenthub.authservice.dto.request.RegisterRequest;
import com.equipmenthub.authservice.dto.request.RegistrationType;
import com.equipmenthub.authservice.dto.request.StoreRegistrationReviewRequest;
import com.equipmenthub.authservice.dto.request.StoreRegistrationDecision;
import com.equipmenthub.authservice.dto.response.IssuedAuthSession;
import com.equipmenthub.authservice.dto.response.AccountSummaryResponse;
import com.equipmenthub.authservice.dto.response.LoginResponse;
import com.equipmenthub.authservice.dto.response.RegisterResponse;
import com.equipmenthub.authservice.dto.response.RotatedRefreshToken;
import com.equipmenthub.authservice.dto.response.StoreRegistrationResponse;
import com.equipmenthub.authservice.dto.response.StoreRegistrationStatus;
import com.equipmenthub.authservice.dto.response.StoreRegistrationSummaryResponse;
import com.equipmenthub.authservice.dto.response.StoreProfileResponse;
import com.equipmenthub.authservice.client.StoreServiceClient;
import com.equipmenthub.authservice.entity.AccountStatus;
import com.equipmenthub.authservice.entity.AuthRole;
import com.equipmenthub.authservice.entity.AuthUser;
import com.equipmenthub.authservice.entity.StoreRegistration;
import com.equipmenthub.authservice.exception.AuthException;
import com.equipmenthub.authservice.repository.AuthUserRepository;
import com.equipmenthub.authservice.repository.StoreRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

@Service
public class AuthService {

	private static final String TOKEN_TYPE = "Bearer";

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private RefreshTokenService refreshTokenService;

	@Autowired
	private AuthUserRepository authUserRepository;

	@Autowired
	private StoreRegistrationRepository storeRegistrationRepository;

	@Autowired
	private AuthRegistrationService authRegistrationService;

	@Autowired
	private StoreServiceClient storeServiceClient;

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
			throw new AuthException(AuthException.Kind.FORBIDDEN,
					"Your store account is awaiting review.");
		}
		if (user.getAccountStatus() == AccountStatus.REJECTED) {
			throw new AuthException(
					AuthException.Kind.FORBIDDEN,
					"The store registration was not approved. Contact support for assistance.");
		}

		LoginResponse loginResponse = createLoginResponse(jwtService.createAccessToken(authentication));
		RefreshTokenService.CreatedRefreshToken refreshToken = refreshTokenService
				.create(UUID.fromString(authentication.getName()));
		return new IssuedAuthSession(loginResponse, refreshToken.getToken(), refreshToken.getExpiresAt());
	}

	public RegisterResponse register(RegisterRequest request) {
		if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new AuthException(
					AuthException.Kind.BAD_REQUEST, "Please check the submitted information.");
		}
		validateRegistrationDetails(request);

		if (request.getAccountType() == RegistrationType.CUSTOMER) {
			return authRegistrationService.registerCustomer(request);
		}

		AuthRegistrationService.PreparedStoreRegistration prepared =
				authRegistrationService.prepareStoreRegistration(request);
		StoreProfileResponse store = storeServiceClient.createProfile(prepared.toRequest());
		if (store == null || store.getStoreId() == null || !prepared.accountId().equals(store.getAccountId())) {
			throw new AuthException(AuthException.Kind.SERVICE_UNAVAILABLE,
					"Store setup is temporarily unavailable. Retry the same registration shortly.");
		}
		return authRegistrationService.completeStoreRegistration(prepared.registrationId(), store.getStoreId());
	}

	@Transactional(readOnly = true)
	public List<StoreRegistrationSummaryResponse> getStoreRegistrations() {
		return storeRegistrationRepository.findAllByStoreIdIsNotNullOrderBySubmittedAtDesc().stream()
				.map(this::toStoreRegistrationSummaryResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<AccountSummaryResponse> getAccounts() {
		return authUserRepository.findAllByRoleInOrderByEmailAsc(
				List.of(AuthRole.CUSTOMER, AuthRole.STORE_OWNER)).stream()
				.map(this::toAccountSummaryResponse)
				.toList();
	}

	@Transactional
	public AccountSummaryResponse updateAccountEnabled(UUID accountId, AccountEnabledUpdateRequest request) {
		AuthUser user = authUserRepository.findById(accountId)
				.filter(account -> account.getRole() == AuthRole.CUSTOMER || account.getRole() == AuthRole.STORE_OWNER)
				.orElseThrow(() -> new AuthException(
						AuthException.Kind.NOT_FOUND, "Account was not found"));
		user.setEnabled(request.getEnabled());
		if (!request.getEnabled()) {
			refreshTokenService.revokeAllForUser(user.getId());
		}
		return toAccountSummaryResponse(user);
	}

	@Transactional(readOnly = true)
	public List<UUID> getApprovedStoreIds() {
		return storeRegistrationRepository.findStoreIdsByAccountStatus(AccountStatus.ACTIVE);
	}

	@Transactional(readOnly = true)
	public StoreRegistrationResponse getStoreRegistration(UUID registrationId) {
		return storeRegistrationRepository.findByIdAndStoreIdIsNotNull(registrationId)
				.map(this::toStoreRegistrationResponse)
				.orElseThrow(() -> new AuthException(
						AuthException.Kind.NOT_FOUND, "Store registration was not found"));
	}

	@Transactional
	public StoreRegistrationResponse reviewStoreRegistration(
			UUID registrationId, UUID adminId, StoreRegistrationReviewRequest request) {
		StoreRegistration registration = findPendingRegistration(registrationId);
		if (request.getStatus() == StoreRegistrationDecision.REJECTED && isBlank(request.getReason())) {
			throw new AuthException(
					AuthException.Kind.BAD_REQUEST, "A reason is required when rejecting a store registration");
		}
		registration.getUser().setAccountStatus(request.getStatus() == StoreRegistrationDecision.APPROVED
				? AccountStatus.ACTIVE : AccountStatus.REJECTED);
		registration.setReviewedAt(Instant.now());
		registration.setReviewedBy(adminId);
		registration.setRejectionReason(request.getStatus() == StoreRegistrationDecision.REJECTED
				? normalizeOptional(request.getReason()) : null);
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
				throw new AuthException(
						AuthException.Kind.BAD_REQUEST, "Please check the submitted information.");
			}
			return;
		}
		if (isBlank(request.getStoreName()) || isBlank(request.getPhone()) || isBlank(request.getCity())
				|| isBlank(request.getDistrict()) || isBlank(request.getAddressLine())) {
			throw new AuthException(
					AuthException.Kind.BAD_REQUEST, "Please check the submitted information.");
		}
	}

	private StoreRegistration findPendingRegistration(UUID registrationId) {
		StoreRegistration registration = storeRegistrationRepository.findById(registrationId)
				.orElseThrow(() -> new AuthException(
						AuthException.Kind.NOT_FOUND, "Store registration was not found"));
		if (registration.getUser().getAccountStatus() != AccountStatus.PENDING) {
			throw new AuthException(AuthException.Kind.CONFLICT, "Store registration is no longer pending");
		}
		if (registration.getStoreId() == null) {
			throw new AuthException(AuthException.Kind.CONFLICT, "Store profile is not ready for review");
		}
		return registration;
	}

	private StoreRegistrationResponse toStoreRegistrationResponse(StoreRegistration registration) {
		AuthUser user = registration.getUser();
		return StoreRegistrationResponse.builder()
				.id(registration.getId())
				.accountId(user.getId())
				.email(user.getEmail())
				.storeId(registration.getStoreId())
				.storeName(registration.getStoreName())
				.phone(registration.getPhone())
				.city(registration.getCity())
				.district(registration.getDistrict())
				.addressLine(registration.getAddressLine())
				.status(toRegistrationStatus(user.getAccountStatus()))
				.submittedAt(registration.getSubmittedAt())
				.reviewedAt(registration.getReviewedAt())
				.reviewedBy(registration.getReviewedBy())
				.rejectionReason(registration.getRejectionReason())
				.build();
	}

	private StoreRegistrationStatus toRegistrationStatus(AccountStatus accountStatus) {
		return switch (accountStatus) {
			case PENDING -> StoreRegistrationStatus.PENDING;
			case ACTIVE -> StoreRegistrationStatus.APPROVED;
			case REJECTED -> StoreRegistrationStatus.REJECTED;
		};
	}

	private StoreRegistrationSummaryResponse toStoreRegistrationSummaryResponse(StoreRegistration registration) {
		AuthUser user = registration.getUser();
		return StoreRegistrationSummaryResponse.builder()
				.id(registration.getId())
				.storeId(registration.getStoreId())
				.email(user.getEmail())
				.storeName(registration.getStoreName())
				.city(registration.getCity())
				.district(registration.getDistrict())
				.status(toRegistrationStatus(user.getAccountStatus()))
				.submittedAt(registration.getSubmittedAt())
				.reviewedAt(registration.getReviewedAt())
				.build();
	}

	private AccountSummaryResponse toAccountSummaryResponse(AuthUser user) {
		return AccountSummaryResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.role(user.getRole())
				.accountStatus(user.getAccountStatus())
				.enabled(user.isEnabled())
				.firstName(user.getFirstName())
				.lastName(user.getLastName())
				.build();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private String normalizeOptional(String value) {
		return isBlank(value) ? null : value.trim();
	}

}
