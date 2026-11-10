package com.equipmenthub.adminservice.exception;

import org.springframework.http.HttpStatusCode;

public class AdminException extends RuntimeException {

	private final HttpStatusCode status;

	public AdminException(HttpStatusCode status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatusCode getStatus() {
		return status;
	}

}
