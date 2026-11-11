package com.equipmenthub.adminservice.dto.response;

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
public class StoreRegistrationSummaryResponse {

	private UUID id;
	private UUID storeId;
	private String email;
	private String storeName;
	private String city;
	private String district;
	private String status;
	private Instant submittedAt;
	private Instant reviewedAt;

}
