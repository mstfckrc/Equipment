package com.equipmenthub.catalogservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

	@Bean
	SecurityFilterChain catalogSecurityFilterChain(
			HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/api/catalog/**").authenticated()
				.anyRequest().denyAll())
			.oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt ->
				jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
		return http.build();
	}

}
