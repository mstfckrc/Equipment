package com.equipmenthub.authservice.dto.response;

import lombok.Getter;

@Getter
public class CsrfTokenResponse {

	private final String token;
	private final String headerName;

	public CsrfTokenResponse(String token, String headerName) {
		this.token = token;
		this.headerName = headerName;
	}

}
