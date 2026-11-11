package com.equipmenthub.adminservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CatalogServiceClientConfiguration {

	@Bean
	RestClient catalogRestClient(
			RestClient.Builder builder, @Value("${catalog.service.base-url}") String baseUrl) {
		return builder.clone().baseUrl(baseUrl).build();
	}

}
