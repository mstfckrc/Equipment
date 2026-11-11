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
public class StoreProfileResponse {

	private UUID storeId;
	private UUID accountId;
	private String storeName;
	private String phone;
	private String city;
	private String district;
	private String addressLine;
	private StoreStatus status;

	public static StoreProfileResponse from(StoreProfile profile) {
		return StoreProfileResponse.builder()
				.storeId(profile.getId())
				.accountId(profile.getOwnerAccountId())
				.storeName(profile.getStoreName())
				.phone(profile.getPhone())
				.city(profile.getCity())
				.district(profile.getDistrict())
				.addressLine(profile.getAddressLine())
				.status(profile.getStatus())
				.build();
	}

}
