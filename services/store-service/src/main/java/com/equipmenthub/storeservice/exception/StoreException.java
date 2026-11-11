package com.equipmenthub.storeservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class StoreException extends RuntimeException {

	private final HttpStatus status;

	public StoreException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

}
