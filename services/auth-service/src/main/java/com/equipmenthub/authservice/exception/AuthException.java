package com.equipmenthub.authservice.exception;

import org.springframework.http.HttpStatus;

public class AuthException extends RuntimeException {

	private final Kind kind;

	public AuthException(Kind kind, String message) {
		super(message);
		this.kind = kind;
	}

	public Kind getKind() {
		return kind;
	}

	public enum Kind {

		BAD_REQUEST(HttpStatus.BAD_REQUEST),
		UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
		FORBIDDEN(HttpStatus.FORBIDDEN),
		NOT_FOUND(HttpStatus.NOT_FOUND),
		CONFLICT(HttpStatus.CONFLICT);

		private final HttpStatus status;

		Kind(HttpStatus status) {
			this.status = status;
		}

		public HttpStatus getStatus() {
			return status;
		}

	}

}
