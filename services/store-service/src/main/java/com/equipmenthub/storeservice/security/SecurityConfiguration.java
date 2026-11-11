package com.equipmenthub.storeservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

	@Bean
	@Order(1)
	SecurityFilterChain storeProvisioningSecurityFilterChain(HttpSecurity http) throws Exception {
		http
			.securityMatcher("/api/stores/internal/profiles")
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(HttpMethod.POST, "/api/stores/internal/profiles").hasRole("AUTH_SERVICE")
				.anyRequest().denyAll())
			.httpBasic(Customizer.withDefaults());
		return http.build();
	}

	@Bean
	UserDetailsService internalServiceUserDetailsService(
			@Value("${store.service.auth-username}") String username,
			@Value("${store.service.auth-password}") String password,
			PasswordEncoder passwordEncoder) {
		if (username.isBlank() || password.isBlank()) {
			throw new IllegalStateException("Auth-to-Store service credentials must be configured");
		}
		return new InMemoryUserDetailsManager(User.withUsername(username)
				.password(passwordEncoder.encode(password))
				.roles("AUTH_SERVICE")
				.build());
	}

	@Bean
	PasswordEncoder internalServicePasswordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	@Order(2)
	SecurityFilterChain storeSecurityFilterChain(
			HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/api/stores/admin/**").hasRole("PLATFORM_ADMIN")
				.requestMatchers("/api/stores/internal/ownership").hasRole("STORE_OWNER")
				.requestMatchers("/api/stores/me").hasRole("STORE_OWNER")
				.requestMatchers(HttpMethod.GET, "/api/stores", "/api/stores/*").hasRole("CUSTOMER")
				.anyRequest().denyAll())
			.oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt ->
				jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
		return http.build();
	}

}
