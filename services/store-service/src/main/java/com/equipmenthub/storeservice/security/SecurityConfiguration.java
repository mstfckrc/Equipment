package com.equipmenthub.storeservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

	@Bean
	SecurityFilterChain storeSecurityFilterChain(
			HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/api/stores/admin/**").hasRole("PLATFORM_ADMIN")
				.requestMatchers(HttpMethod.POST, "/api/stores/applications").hasRole("STORE_OWNER")
				.requestMatchers("/api/stores/applications/**", "/api/stores/owner/**")
					.hasRole("STORE_OWNER")
				.requestMatchers("/api/stores/internal/**").hasRole("STORE_OWNER")
				.anyRequest().denyAll())
			.oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt ->
				jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
		return http.build();
	}

}
