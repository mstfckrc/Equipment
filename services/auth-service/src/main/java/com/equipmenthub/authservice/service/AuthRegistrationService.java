package com.equipmenthub.authservice.service;

import com.equipmenthub.authservice.dto.request.RegisterRequest;
import com.equipmenthub.authservice.dto.request.RegistrationType;
import com.equipmenthub.authservice.dto.request.StoreProfileProvisionRequest;
import com.equipmenthub.authservice.dto.response.RegisterResponse;
import com.equipmenthub.authservice.entity.AccountStatus;
import com.equipmenthub.authservice.entity.AuthRole;
import com.equipmenthub.authservice.entity.AuthUser;
import com.equipmenthub.authservice.entity.StoreRegistration;
import com.equipmenthub.authservice.exception.AuthException;
import com.equipmenthub.authservice.repository.AuthUserRepository;
import com.equipmenthub.authservice.repository.StoreRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class AuthRegistrationService {

	@Autowired
	private AuthUserRepository authUserRepository;

	@Autowired
	private StoreRegistrationRepository storeRegistrationRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Transactional
	public RegisterResponse registerCustomer(RegisterRequest request) {
		String email = normalizeEmail(request.getEmail());
		if (authUserRepository.existsByEmailIgnoreCase(email)) {
			throw registrationConflict();
		}
		AuthUser user = new AuthUser(email, passwordEncoder.encode(request.getPassword()), AuthRole.CUSTOMER,
				normalizeOptional(request.getFirstName()), normalizeOptional(request.getLastName()));
		user.setAccountStatus(AccountStatus.ACTIVE);
		user = authUserRepository.save(user);
		return RegisterResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.accountType(RegistrationType.CUSTOMER)
				.status(AccountStatus.ACTIVE)
				.message("Account created. You can now sign in.")
				.build();
	}

	@Transactional
	public PreparedStoreRegistration prepareStoreRegistration(RegisterRequest request) {
		String email = normalizeEmail(request.getEmail());
		AuthUser existingUser = authUserRepository.findByEmailIgnoreCase(email).orElse(null);
		if (existingUser != null) {
			return resumeIncompleteRegistration(existingUser, request);
		}

		AuthUser user = new AuthUser(email, passwordEncoder.encode(request.getPassword()), AuthRole.STORE_OWNER);
		user.setAccountStatus(AccountStatus.PENDING);
		user = authUserRepository.save(user);
		StoreRegistration registration = storeRegistrationRepository.save(new StoreRegistration(user,
				request.getStoreName(), request.getPhone(), request.getCity(), request.getDistrict(),
				request.getAddressLine()));
		return prepared(registration);
	}

	@Transactional
	public RegisterResponse completeStoreRegistration(UUID registrationId, UUID storeId) {
		StoreRegistration registration = storeRegistrationRepository.findById(registrationId)
				.orElseThrow(AuthRegistrationService::registrationConflict);
		if (registration.getStoreId() != null && !registration.getStoreId().equals(storeId)) {
			throw registrationConflict();
		}
		registration.setStoreId(storeId);
		AuthUser user = registration.getUser();
		return RegisterResponse.builder()
				.id(user.getId())
				.registrationId(registrationId)
				.storeId(storeId)
				.email(user.getEmail())
				.accountType(RegistrationType.STORE_OWNER)
				.status(AccountStatus.PENDING)
				.message("Store registration submitted and is awaiting review.")
				.build();
	}

	private PreparedStoreRegistration resumeIncompleteRegistration(AuthUser user, RegisterRequest request) {
		if (user.getRole() != AuthRole.STORE_OWNER || user.getAccountStatus() != AccountStatus.PENDING) {
			throw registrationConflict();
		}
		StoreRegistration registration = storeRegistrationRepository.findByUserEmailIgnoreCase(user.getEmail())
				.orElseThrow(AuthRegistrationService::registrationConflict);
		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			throw registrationConflict();
		}
		if (!same(request.getStoreName(), registration.getStoreName())
				|| !same(request.getPhone(), registration.getPhone())
				|| !same(request.getCity(), registration.getCity())
				|| !same(request.getDistrict(), registration.getDistrict())
				|| !same(request.getAddressLine(), registration.getAddressLine())) {
			throw registrationConflict();
		}
		return prepared(registration);
	}

	private PreparedStoreRegistration prepared(StoreRegistration registration) {
		return new PreparedStoreRegistration(registration.getId(), registration.getUser().getId(),
				registration.getStoreName(), registration.getPhone(),
				registration.getCity(), registration.getDistrict(), registration.getAddressLine());
	}

	private static String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private static String normalizeOptional(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private static boolean same(String first, String second) {
		return first != null && second != null && first.trim().equals(second);
	}

	private static AuthException registrationConflict() {
		return new AuthException(AuthException.Kind.CONFLICT,
				"This registration could not be completed. Check your details or sign in.");
	}

	public record PreparedStoreRegistration(UUID registrationId, UUID accountId, String storeName,
			String phone, String city, String district, String addressLine) {
		public StoreProfileProvisionRequest toRequest() {
			return StoreProfileProvisionRequest.builder()
					.accountId(accountId)
					.storeName(storeName)
					.phone(phone)
					.city(city)
					.district(district)
					.addressLine(addressLine)
					.build();
		}
	}

}
