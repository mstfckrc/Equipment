package com.equipmenthub.authservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthException extends RuntimeException {

	private final Kind kind;

	public AuthException(Kind kind, String message) {
		super(message);
		this.kind = kind;
	}

	@Getter
	public enum Kind {

		BAD_REQUEST(HttpStatus.BAD_REQUEST),
		UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
		FORBIDDEN(HttpStatus.FORBIDDEN),
		NOT_FOUND(HttpStatus.NOT_FOUND),
		CONFLICT(HttpStatus.CONFLICT),
		SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE);

		private final HttpStatus status;

		Kind(HttpStatus status) {
			this.status = status;
		}

	}

}
