package com.equipmenthub.adminservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AuthServiceClientConfiguration {

	@Bean
	RestClient authRestClient(
			RestClient.Builder builder, @Value("${auth.service.base-url}") String baseUrl) {
		return builder.baseUrl(baseUrl).build();
	}

}
