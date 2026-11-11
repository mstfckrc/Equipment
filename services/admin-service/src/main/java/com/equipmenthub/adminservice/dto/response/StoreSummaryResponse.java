package com.equipmenthub.adminservice.dto.response;

import com.equipmenthub.adminservice.dto.request.StoreStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreSummaryResponse {

	private UUID storeId;
	private UUID accountId;
	private String storeName;
	private String phone;
	private String city;
	private String district;
	private StoreStatus status;

}
