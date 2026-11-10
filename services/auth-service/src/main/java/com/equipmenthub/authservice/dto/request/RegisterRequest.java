package com.equipmenthub.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {

	@NotNull
	private RegistrationType accountType;

	@NotBlank
	@Email
	@Size(max = 254)
	private String email;

	@NotBlank
	@Size(min = 12, max = 64)
	private String password;

	@Size(max = 100)
	private String firstName;

	@Size(max = 100)
	private String lastName;

	@Size(max = 200)
	private String storeName;

	@Size(max = 32)
	private String phone;

	@Size(max = 100)
	private String city;

	@Size(max = 100)
	private String district;

	@Size(max = 500)
	private String addressLine;

}
