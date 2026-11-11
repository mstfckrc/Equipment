package com.equipmenthub.catalogservice.dto.request;

import com.equipmenthub.catalogservice.entity.ProductStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OwnerProductStatusRequest {

	@NotNull
	private ProductStatus status;

}
