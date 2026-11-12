package com.equipmenthub.rentalservice.exception;

import com.equipmenthub.rentalservice.dto.response.ApiErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(RentalException.class)
	ResponseEntity<ApiErrorResponse> rentalException(RentalException exception) {
		return ResponseEntity.status(exception.getStatus())
				.body(ApiErrorResponse.builder()
						.code(exception.getCode())
						.message(exception.getMessage())
						.priceChanges(exception.getPriceChanges())
						.availabilityConflicts(exception.getAvailabilityConflicts())
						.build());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ResponseEntity<ApiErrorResponse> conflict() {
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(ApiErrorResponse.builder().code("CONFLICT")
					.message("The request could not be saved due to a conflicting update")
					.build());
	}

	@ExceptionHandler({ MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
			MissingRequestHeaderException.class,
			MissingServletRequestParameterException.class,
			MethodArgumentTypeMismatchException.class })
	ResponseEntity<ApiErrorResponse> invalidRequest() {
		return ResponseEntity.badRequest().body(ApiErrorResponse.builder()
				.code("INVALID_REQUEST")
				.message("Please check the submitted information")
				.build());
	}

}
