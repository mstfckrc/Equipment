package com.equipmenthub.catalogservice.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.equipmenthub.catalogservice")
@EntityScan("com.equipmenthub.catalogservice.entity")
@EnableJpaRepositories("com.equipmenthub.catalogservice.repository")
public class CatalogServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatalogServiceApplication.class, args);
	}

}
