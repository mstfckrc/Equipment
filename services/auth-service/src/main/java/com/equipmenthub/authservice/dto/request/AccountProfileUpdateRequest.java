package com.equipmenthub.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccountProfileUpdateRequest {

	@NotBlank
	@Size(max = 100)
	private String firstName;

	@NotBlank
	@Size(max = 100)
	private String lastName;
}
