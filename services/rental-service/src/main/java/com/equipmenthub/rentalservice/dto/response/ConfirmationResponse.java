package com.equipmenthub.rentalservice.dto.response;

import com.equipmenthub.rentalservice.entity.ConfirmationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmationResponse {

	private UUID documentId;
	private UUID checkoutId;
	private ConfirmationStatus status;
	private int contentVersion;
	private Instant preparedAt;
	private String errorCode;
	private String message;
	private ConfirmationContent content;

}
