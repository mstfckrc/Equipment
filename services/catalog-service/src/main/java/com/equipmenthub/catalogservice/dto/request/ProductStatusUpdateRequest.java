package com.equipmenthub.catalogservice.dto.request;

import com.equipmenthub.catalogservice.entity.ProductStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductStatusUpdateRequest {

	@NotNull
	private ProductStatus status;

	@Size(max = 500)
	private String reason;

}
