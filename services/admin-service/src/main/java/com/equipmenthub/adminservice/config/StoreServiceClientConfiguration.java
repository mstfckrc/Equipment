package com.equipmenthub.adminservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class StoreServiceClientConfiguration {

	@Bean
	RestClient storeRestClient(
			RestClient.Builder builder, @Value("${store.service.base-url}") String baseUrl) {
		return builder.clone().baseUrl(baseUrl).build();
	}

}
