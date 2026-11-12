package com.equipmenthub.authservice.dto.response;

import com.equipmenthub.authservice.entity.AccountStatus;
import com.equipmenthub.authservice.entity.AuthRole;
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
	private AuthRole role;
	private AccountStatus accountStatus;
	private boolean enabled;
	private String firstName;
	private String lastName;

}
