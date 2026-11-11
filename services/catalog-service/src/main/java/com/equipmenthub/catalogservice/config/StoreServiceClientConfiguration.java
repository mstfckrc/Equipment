package com.equipmenthub.catalogservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class StoreServiceClientConfiguration {

	@Bean
	RestClient storeRestClient(RestClient.Builder builder, @Value("${equipmenthub.services.store.base-url}") String baseUrl) {
		return builder.baseUrl(baseUrl).build();
	}

}
