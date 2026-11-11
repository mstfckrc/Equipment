package com.equipmenthub.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CsrfTokenResponse {

	private final String token;
	private final String headerName;

}
