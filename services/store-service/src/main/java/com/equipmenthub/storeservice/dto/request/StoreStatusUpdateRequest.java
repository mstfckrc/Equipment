package com.equipmenthub.storeservice.dto.request;

import com.equipmenthub.storeservice.entity.StoreStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StoreStatusUpdateRequest {

	@NotNull
	private StoreStatus status;

}
