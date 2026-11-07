package com.equipmenthub.authservice.exception;

public class InvalidRegistrationException extends RuntimeException {

	public InvalidRegistrationException() {
		super("Registration request is invalid");
	}

}
