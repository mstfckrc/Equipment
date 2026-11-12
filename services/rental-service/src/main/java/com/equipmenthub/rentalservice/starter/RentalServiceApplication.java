package com.equipmenthub.rentalservice.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.equipmenthub.rentalservice")
@EntityScan("com.equipmenthub.rentalservice.entity")
@EnableJpaRepositories("com.equipmenthub.rentalservice.repository")
public class RentalServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentalServiceApplication.class, args);
	}

}
