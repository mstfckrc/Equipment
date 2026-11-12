package com.equipmenthub.rentalservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ServiceClientConfiguration {

	@Bean
	RestClient storeRestClient(
			RestClient.Builder builder, @Value("${equipmenthub.services.store.base-url}") String baseUrl) {
		return builder.clone().baseUrl(baseUrl).build();
	}

	@Bean
	RestClient catalogRestClient(
			RestClient.Builder builder, @Value("${equipmenthub.services.catalog.base-url}") String baseUrl) {
		return builder.clone().baseUrl(baseUrl).build();
	}

}
