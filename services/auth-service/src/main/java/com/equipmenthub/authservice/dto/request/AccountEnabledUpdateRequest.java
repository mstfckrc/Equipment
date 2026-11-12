package com.equipmenthub.authservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountEnabledUpdateRequest {

	@NotNull
	private Boolean enabled;

}
