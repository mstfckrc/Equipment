package com.equipmenthub.authservice.exception;

public class EmailAlreadyExistsException extends RuntimeException {

	public EmailAlreadyExistsException() {
		super("Registration could not be completed");
	}

}
