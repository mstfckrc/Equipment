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
public class PublicStoreResponse {

	private UUID storeId;
	private String storeName;
	private String phone;
	private String city;
	private String district;
	private String addressLine;

	public static PublicStoreResponse from(StoreProfile profile) {
		return PublicStoreResponse.builder()
				.storeId(profile.getId())
				.storeName(profile.getStoreName())
				.phone(profile.getPhone())
				.city(profile.getCity())
				.district(profile.getDistrict())
				.addressLine(profile.getAddressLine())
				.build();
	}

}
