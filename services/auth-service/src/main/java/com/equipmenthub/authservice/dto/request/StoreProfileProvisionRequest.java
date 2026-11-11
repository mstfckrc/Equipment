package com.equipmenthub.authservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreProfileProvisionRequest {

	private UUID accountId;
	private String storeName;
	private String phone;
	private String city;
	private String district;
	private String addressLine;

}
