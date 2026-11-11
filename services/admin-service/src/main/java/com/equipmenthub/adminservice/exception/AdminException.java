package com.equipmenthub.adminservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class AdminException extends RuntimeException {

	private final HttpStatusCode status;

	public AdminException(HttpStatusCode status, String message) {
		super(message);
		this.status = status;
	}

}
