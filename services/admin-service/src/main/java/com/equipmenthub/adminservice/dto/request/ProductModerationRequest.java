package com.equipmenthub.adminservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductModerationRequest {

	@NotNull
	private AdminProductStatus status;

	@Size(max = 500)
	private String reason;

}
