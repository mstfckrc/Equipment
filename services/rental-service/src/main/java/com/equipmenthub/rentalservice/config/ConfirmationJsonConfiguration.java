package com.equipmenthub.rentalservice.config;

import org.hibernate.type.format.jackson.Jackson3JsonFormatMapper;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class ConfirmationJsonConfiguration {

	@Bean
	HibernatePropertiesCustomizer confirmationJsonMapping(JsonMapper jsonMapper) {
		return properties -> properties.put("hibernate.type.json_format_mapper", new Jackson3JsonFormatMapper(jsonMapper));
	}

}
