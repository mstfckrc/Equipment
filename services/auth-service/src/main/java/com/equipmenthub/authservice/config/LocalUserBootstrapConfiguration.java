package com.equipmenthub.authservice.config;

import com.equipmenthub.authservice.entity.AuthRole;
import com.equipmenthub.authservice.entity.AuthUser;
import com.equipmenthub.authservice.repository.AuthUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@ConditionalOnProperty(prefix = "auth.local-user", name = "enabled", havingValue = "true")
public class LocalUserBootstrapConfiguration {

	@Bean
	ApplicationRunner createLocalUser(
			AuthUserRepository authUserRepository,
			PasswordEncoder passwordEncoder,
			@Value("${auth.local-user.email:}") String email,
			@Value("${auth.local-user.password:}") String password) {
		return arguments -> {
			if (email.isBlank() || password.isBlank()) {
				throw new IllegalStateException(
						"Local user bootstrap is enabled, but its email or password is missing");
			}

			if (authUserRepository.findByEmailIgnoreCase(email).isEmpty()) {
				authUserRepository.save(new AuthUser(
						email, passwordEncoder.encode(password), AuthRole.CUSTOMER));
			}
		};
	}

}
