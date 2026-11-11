package com.equipmenthub.authservice.dto.response;

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
public class StoreRegistrationResponse {

	private UUID id;
	private UUID accountId;
	private UUID storeId;
	private String email;
	private String storeName;
	private String phone;
	private String city;
	private String district;
	private String addressLine;
	private StoreRegistrationStatus status;
	private Instant submittedAt;
	private Instant reviewedAt;
	private UUID reviewedBy;
	private String rejectionReason;

}
