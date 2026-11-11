package com.equipmenthub.storeservice.dto.response;

import com.equipmenthub.storeservice.entity.StoreProfile;
import com.equipmenthub.storeservice.entity.StoreStatus;
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

	public static StoreSummaryResponse from(StoreProfile profile) {
		return StoreSummaryResponse.builder()
				.storeId(profile.getId())
				.accountId(profile.getOwnerAccountId())
				.storeName(profile.getStoreName())
				.phone(profile.getPhone())
				.city(profile.getCity())
				.district(profile.getDistrict())
				.status(profile.getStatus())
				.build();
	}

}
