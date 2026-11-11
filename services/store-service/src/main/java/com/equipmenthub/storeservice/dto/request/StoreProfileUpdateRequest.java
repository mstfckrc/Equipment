package com.equipmenthub.storeservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StoreProfileUpdateRequest {

	@NotBlank
	@Size(max = 200)
	private String storeName;

	@NotBlank
	@Size(max = 32)
	private String phone;

	@NotBlank
	@Size(max = 100)
	private String city;

	@NotBlank
	@Size(max = 100)
	private String district;

	@NotBlank
	@Size(max = 500)
	private String addressLine;

}
