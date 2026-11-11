package com.equipmenthub.authservice.exception;

import com.equipmenthub.authservice.dto.response.ApiErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(AuthException.class)
	ResponseEntity<ApiErrorResponse> authException(AuthException exception) {
		return error(exception.getKind().getStatus(), exception.getMessage());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ResponseEntity<ApiErrorResponse> registrationConflict() {
		return error(HttpStatus.CONFLICT,
				"This registration could not be completed. Check your details or sign in.");
	}

	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	ResponseEntity<ApiErrorResponse> concurrentReview() {
		return error(HttpStatus.CONFLICT, "This store registration has already been reviewed");
	}

	@ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
	ResponseEntity<ApiErrorResponse> invalidRequest() {
		return error(HttpStatus.BAD_REQUEST, "Please check the submitted information.");
	}

	@ExceptionHandler(InternalAuthenticationServiceException.class)
	ResponseEntity<ApiErrorResponse> authenticationServiceFailure() {
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "Authentication could not be completed");
	}

	private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(new ApiErrorResponse(message));
	}

}
