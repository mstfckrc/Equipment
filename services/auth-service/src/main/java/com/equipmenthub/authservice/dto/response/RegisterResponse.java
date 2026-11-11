package com.equipmenthub.authservice.dto.response;

import com.equipmenthub.authservice.dto.request.RegistrationType;
import com.equipmenthub.authservice.entity.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {

	private UUID id;
	private UUID registrationId;
	private UUID storeId;
	private String email;
	private RegistrationType accountType;
	private AccountStatus status;
	private String message;

}
