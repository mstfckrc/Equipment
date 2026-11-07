package com.equipmenthub.authservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;

@Configuration
public class AuthSecurityConfiguration {

	@Bean
	SecurityFilterChain authApplicationSecurityFilterChain(
			HttpSecurity http, CsrfTokenRepository csrfTokenRepository) throws Exception {
		http
			.csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository))
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.requestCache(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(HttpMethod.GET, "/csrf").permitAll()
				.requestMatchers(HttpMethod.POST, "/login", "/register", "/refresh", "/logout").permitAll()
				.anyRequest().denyAll())
			.exceptionHandling(exceptions -> exceptions.accessDeniedHandler((request, response, exception) -> {
				response.setStatus(403);
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				response.getWriter().write("{\"message\":\"Request rejected\"}");
			}))
			.oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));

		return http.build();
	}

	@Bean
	CsrfTokenRepository csrfTokenRepository(@Value("${auth.refresh-cookie.secure:false}") boolean secure) {
		CookieCsrfTokenRepository repository = new CookieCsrfTokenRepository();
		repository.setCookiePath("/auth");
		repository.setCookieCustomizer(cookie -> cookie.path("/auth").sameSite("Lax").secure(secure));
		return repository;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

}
