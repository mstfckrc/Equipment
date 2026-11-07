package com.equipmenthub.authservice.controller;

import com.equipmenthub.authservice.dto.response.ApiErrorResponse;
import com.equipmenthub.authservice.exception.EmailAlreadyExistsException;
import com.equipmenthub.authservice.exception.InvalidCredentialsException;
import com.equipmenthub.authservice.exception.InvalidRefreshTokenException;
import com.equipmenthub.authservice.exception.InvalidRegistrationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(InvalidCredentialsException.class)
	ResponseEntity<ApiErrorResponse> invalidCredentials() {
		return error(HttpStatus.UNAUTHORIZED, "Invalid email or password");
	}

	@ExceptionHandler(InvalidRefreshTokenException.class)
	ResponseEntity<ApiErrorResponse> invalidRefreshToken() {
		return error(HttpStatus.UNAUTHORIZED, "Refresh session is invalid or expired");
	}

	@ExceptionHandler({EmailAlreadyExistsException.class, DataIntegrityViolationException.class})
	ResponseEntity<ApiErrorResponse> registrationConflict() {
		return error(HttpStatus.CONFLICT, "Registration could not be completed");
	}

	@ExceptionHandler({InvalidRegistrationException.class, MethodArgumentNotValidException.class,
			HttpMessageNotReadableException.class})
	ResponseEntity<ApiErrorResponse> invalidRequest() {
		return error(HttpStatus.BAD_REQUEST, "Invalid request");
	}

	@ExceptionHandler(InternalAuthenticationServiceException.class)
	ResponseEntity<ApiErrorResponse> authenticationServiceFailure() {
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "Authentication could not be completed");
	}

	private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(new ApiErrorResponse(message));
	}

}
