package com.equipmenthub.authservice.config;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, PlatformAdminBootstrapProperties.class})
public class JwtConfiguration {

	@Bean
	SecretKey jwtSecretKey(JwtProperties jwtProperties) {
		try {
			byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getSecret());
			if (keyBytes.length < 32) {
				throw new IllegalStateException("AUTH_JWT_SECRET must decode to at least 32 bytes");
			}
			return new SecretKeySpec(keyBytes, "HmacSHA256");
		}
		catch (IllegalArgumentException exception) {
			throw new IllegalStateException("AUTH_JWT_SECRET must be valid Base64", exception);
		}
	}

	@Bean
	JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
		return NimbusJwtEncoder.withSecretKey(jwtSecretKey)
				.algorithm(MacAlgorithm.HS256)
				.build();
	}

}
