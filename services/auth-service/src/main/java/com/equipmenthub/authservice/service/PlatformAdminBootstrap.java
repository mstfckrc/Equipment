package com.equipmenthub.authservice.service;

import com.equipmenthub.authservice.config.PlatformAdminBootstrapProperties;
import com.equipmenthub.authservice.dto.request.RegisterRequest;
import com.equipmenthub.authservice.dto.request.RegistrationType;
import com.equipmenthub.authservice.entity.AuthRole;
import com.equipmenthub.authservice.entity.AuthUser;
import com.equipmenthub.authservice.entity.AccountStatus;
import com.equipmenthub.authservice.repository.AuthUserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PlatformAdminBootstrap implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(PlatformAdminBootstrap.class);
	private static final int MAX_BCRYPT_PASSWORD_BYTES = 72;

	private final AuthUserRepository authUserRepository;
	private final PlatformAdminBootstrapProperties properties;
	private final PasswordEncoder passwordEncoder;
	private final Validator validator;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (authUserRepository.existsByRole(AuthRole.PLATFORM_ADMIN)) {
			logger.info("Platform admin already exists; initial account bootstrap skipped.");
			return;
		}

		String email = properties.getEmail() == null ? "" : properties.getEmail().trim();
		String password = properties.getPassword();
		if (email.isBlank() || password == null || password.isBlank()) {
			throw new IllegalStateException(
					"No PLATFORM_ADMIN exists. Set PLATFORM_ADMIN_EMAIL and PLATFORM_ADMIN_PASSWORD for the initial setup.");
		}

		RegisterRequest credentials = new RegisterRequest();
		credentials.setAccountType(RegistrationType.CUSTOMER);
		credentials.setEmail(email);
		credentials.setPassword(password);
		Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(credentials);
		if (!violations.isEmpty() || password.getBytes(StandardCharsets.UTF_8).length > MAX_BCRYPT_PASSWORD_BYTES) {
			throw new IllegalStateException(
					"Platform admin bootstrap credentials do not meet the existing registration requirements.");
		}

		String normalizedEmail = email.toLowerCase(Locale.ROOT);
		Optional<AuthUser> existingAccount = authUserRepository.findByEmailIgnoreCase(normalizedEmail);
		if (existingAccount.isPresent()) {
			AuthUser user = existingAccount.get();
			if (!passwordEncoder.matches(password, user.getPasswordHash())) {
				throw new IllegalStateException(
						"PLATFORM_ADMIN_EMAIL belongs to an existing account with a different password. Bootstrap was stopped.");
			}
			user.setRole(AuthRole.PLATFORM_ADMIN);
			user.setAccountStatus(AccountStatus.ACTIVE);
			authUserRepository.save(user);
			logger.info("Existing account verified and promoted to platform admin.");
			return;
		}

		AuthUser platformAdmin = new AuthUser(
				normalizedEmail,
				passwordEncoder.encode(password),
				AuthRole.PLATFORM_ADMIN);
		authUserRepository.save(platformAdmin);
		logger.info("Initial platform admin account created.");
	}

}
