package com.equipmenthub.rentalservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

	@Bean
	SecurityFilterChain rentalSecurityFilterChain(
			HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/api/rentals/stores/me/**").hasRole("STORE_OWNER")
				.requestMatchers(HttpMethod.GET,
						"/api/rentals/stores/*/listings/*/availability",
						"/api/rentals/reservations/me").hasRole("CUSTOMER")
				.requestMatchers(HttpMethod.POST, "/api/rentals/reservations").hasRole("CUSTOMER")
				.requestMatchers(HttpMethod.PATCH, "/api/rentals/reservations/me/items/**").hasRole("CUSTOMER")
				.requestMatchers("/api/rentals/**").denyAll()
				.anyRequest().denyAll())
			.oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt ->
				jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
		return http.build();
	}

}
