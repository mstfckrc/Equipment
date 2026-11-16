package com.equipmenthub.rentalservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCheckoutRequest {

	@NotEmpty
	@Size(max = 100, message = "A checkout can contain at most 100 item lines")
	private List<@Valid ReservationItemRequest> items;

}
