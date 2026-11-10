package com.equipmenthub.adminservice.exception;

import com.equipmenthub.adminservice.dto.response.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(AdminException.class)
	ResponseEntity<ApiErrorResponse> adminException(AdminException exception) {
		return ResponseEntity.status(exception.getStatus())
				.body(new ApiErrorResponse(exception.getMessage()));
	}

	@ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
			MethodArgumentTypeMismatchException.class})
	ResponseEntity<ApiErrorResponse> invalidRequest() {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ApiErrorResponse("Invalid request"));
	}

}
