package com.equipmenthub.catalogservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductListingRequest {

	@NotBlank
	@Size(max = 200)
	private String name;

	@NotBlank
	@Size(max = 100)
	private String category;

	@NotBlank
	@Size(max = 2000)
	private String description;

	@Size(max = 1000)
	private String imageUrl;

	@NotNull
	@DecimalMin("0.01")
	@Digits(integer = 8, fraction = 2)
	private BigDecimal dailyPrice;

}
