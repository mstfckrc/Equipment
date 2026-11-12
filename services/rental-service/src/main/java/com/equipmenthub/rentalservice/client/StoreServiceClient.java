package com.equipmenthub.rentalservice.client;

import com.equipmenthub.rentalservice.dto.response.StoreOwnershipResponse;
import com.equipmenthub.rentalservice.exception.RentalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.UUID;

@Component
public class StoreServiceClient {

	@Autowired
	@Qualifier("storeRestClient")
	private RestClient storeRestClient;

	public UUID getActiveStoreId(String accessToken) {
		try {
			StoreOwnershipResponse response = storeRestClient.get()
					.uri("/api/stores/internal/ownership")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
					.retrieve()
					.body(StoreOwnershipResponse.class);
			if (response == null || response.getStoreId() == null) {
				throw new RentalException(HttpStatus.BAD_GATEWAY, "Store service returned an invalid response");
			}
			return response.getStoreId();
		}
		catch (RestClientResponseException exception) {
			if (exception.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
				throw new RentalException(HttpStatus.NOT_FOUND, "An active store profile is required");
			}
			if (exception.getStatusCode().is4xxClientError()) {
				throw new RentalException(HttpStatus.FORBIDDEN, "An active store profile is required");
			}
			throw serviceUnavailable();
		}
		catch (ResourceAccessException exception) {
			throw serviceUnavailable();
		}
		catch (RestClientException exception) {
			throw new RentalException(HttpStatus.BAD_GATEWAY, "Store service returned an invalid response");
		}
	}

	private RentalException serviceUnavailable() {
		return new RentalException(HttpStatus.SERVICE_UNAVAILABLE, "Store service is unavailable");
	}

}
