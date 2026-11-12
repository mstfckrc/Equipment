package com.equipmenthub.rentalservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCheckoutRequest {

	@NotEmpty
	private List<@Valid ReservationItemRequest> items;

}
