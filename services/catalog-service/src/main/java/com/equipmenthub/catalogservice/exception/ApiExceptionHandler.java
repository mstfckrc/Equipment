package com.equipmenthub.catalogservice.exception;

import com.equipmenthub.catalogservice.dto.response.ApiErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(CatalogException.class)
	ResponseEntity<ApiErrorResponse> catalogException(CatalogException exception) {
		return ResponseEntity.status(exception.getStatus()).body(new ApiErrorResponse(exception.getMessage()));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ResponseEntity<ApiErrorResponse> conflict() {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(new ApiErrorResponse("Product listing could not be saved"));
	}

	@ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
			MethodArgumentTypeMismatchException.class})
	ResponseEntity<ApiErrorResponse> invalidRequest() {
		return ResponseEntity.badRequest().body(new ApiErrorResponse("Please check the submitted information"));
	}

}
