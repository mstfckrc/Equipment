package com.equipmenthub.rentalservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;
import com.equipmenthub.rentalservice.dto.response.AvailabilityConflictResponse;
import com.equipmenthub.rentalservice.dto.response.PriceChangeResponse;

@Getter
public class RentalException extends RuntimeException {

	private final HttpStatus status;
	private final String code;
	private final List<PriceChangeResponse> priceChanges;
	private final List<AvailabilityConflictResponse> availabilityConflicts;

	public RentalException(HttpStatus status, String message) {
		this(status, status.name(), message, List.of(), List.of());
	}

	public RentalException(HttpStatus status, String code, String message,
			List<PriceChangeResponse> priceChanges, List<AvailabilityConflictResponse> availabilityConflicts) {
		super(message);
		this.status = status;
		this.code = code;
		this.priceChanges = priceChanges == null ? List.of() : List.copyOf(priceChanges);
		this.availabilityConflicts = availabilityConflicts == null ? List.of() : List.copyOf(availabilityConflicts);
	}

}
