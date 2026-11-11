package com.equipmenthub.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class StoreServiceClientConfiguration {

	@Bean
	RestClient storeServiceRestClient(RestClient.Builder builder,
			@Value("${store.service.base-url}") String baseUrl,
			@Value("${store.service.username}") String username,
			@Value("${store.service.password}") String password) {
		String credentials = Base64.getEncoder().encodeToString(
				(username + ":" + password).getBytes(StandardCharsets.UTF_8));
		return builder.baseUrl(baseUrl)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
				.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.build();
	}

}
