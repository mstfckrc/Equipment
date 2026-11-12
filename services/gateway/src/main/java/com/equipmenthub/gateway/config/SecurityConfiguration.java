package com.equipmenthub.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
public class SecurityConfiguration {

	@Bean
	@Order(1)
	SecurityFilterChain authProxySecurityFilterChain(
			HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
		http
			.securityMatcher("/auth/**")
			.cors(Customizer.withDefaults())
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(HttpMethod.GET, "/auth/csrf").permitAll()
				.requestMatchers(HttpMethod.POST,
						"/auth/login", "/auth/register", "/auth/refresh", "/auth/logout").permitAll()
				.requestMatchers("/auth/admin/**").denyAll()
				.anyRequest().denyAll())
			.oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt ->
				jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

		return http.build();
	}

	@Bean
	@Order(2)
	SecurityFilterChain gatewayApiSecurityFilterChain(
			HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
		http
			.cors(Customizer.withDefaults())
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/api/admin/**").hasRole("PLATFORM_ADMIN")
				.requestMatchers("/api/stores/internal/**").denyAll()
				.requestMatchers("/api/stores/admin/**").denyAll()
				.requestMatchers("/api/stores/me").hasRole("STORE_OWNER")
				.requestMatchers(HttpMethod.GET, "/api/stores", "/api/stores/*").hasRole("CUSTOMER")
				.requestMatchers("/api/catalog/stores/me/**").hasRole("STORE_OWNER")
				.requestMatchers("/api/rentals/stores/me/**").hasRole("STORE_OWNER")
				.requestMatchers(HttpMethod.GET,
						"/api/rentals/stores/*/listings/*/availability",
						"/api/rentals/reservations/me").hasRole("CUSTOMER")
				.requestMatchers(HttpMethod.POST, "/api/rentals/reservations").hasRole("CUSTOMER")
				.requestMatchers(HttpMethod.PATCH, "/api/rentals/reservations/me/items/**").hasRole("CUSTOMER")
				.requestMatchers(HttpMethod.GET, "/api/catalog/stores/*/products/**").hasRole("CUSTOMER")
				.requestMatchers("/api/**").denyAll()
				.anyRequest().denyAll())
			.oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt ->
				jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(
			@Value("${gateway.cors.allowed-origin:http://localhost:3000}") String allowedOrigin) {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(allowedOrigin));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN", "Idempotency-Key"));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

}
