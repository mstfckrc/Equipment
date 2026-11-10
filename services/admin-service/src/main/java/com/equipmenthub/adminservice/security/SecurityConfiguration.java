package com.equipmenthub.adminservice.security;

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
	SecurityFilterChain adminSecurityFilterChain(
			HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.requestCache(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/api/admin/**").hasRole("PLATFORM_ADMIN")
				.anyRequest().denyAll())
			.oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt ->
				jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
		return http.build();
	}

}
