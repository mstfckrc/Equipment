package com.equipmenthub.storeservice.dto.response;

import com.equipmenthub.storeservice.entity.StoreProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicStoreSummaryResponse {

	private UUID storeId;
	private String storeName;
	private String city;
	private String district;

	public static PublicStoreSummaryResponse from(StoreProfile profile) {
		return PublicStoreSummaryResponse.builder()
				.storeId(profile.getId())
				.storeName(profile.getStoreName())
				.city(profile.getCity())
				.district(profile.getDistrict())
				.build();
	}

}
