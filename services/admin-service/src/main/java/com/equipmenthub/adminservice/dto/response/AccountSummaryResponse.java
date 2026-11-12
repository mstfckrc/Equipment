package com.equipmenthub.adminservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSummaryResponse {

	private UUID id;
	private String email;
	private String role;
	private String accountStatus;
	private boolean enabled;
	private String firstName;
	private String lastName;

}
