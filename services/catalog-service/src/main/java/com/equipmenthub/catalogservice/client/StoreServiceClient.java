package com.equipmenthub.catalogservice.client;

import com.equipmenthub.catalogservice.dto.response.StoreOwnershipResponse;
import com.equipmenthub.catalogservice.exception.CatalogException;
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
			StoreOwnershipResponse response = storeRestClient.get().uri("/api/stores/internal/ownership")
					.header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
					.retrieve().body(StoreOwnershipResponse.class);
			if (response == null || response.getStoreId() == null) {
				throw new CatalogException(HttpStatus.BAD_GATEWAY, "Store service returned an invalid response");
			}
			return response.getStoreId();
		}
		catch (RestClientResponseException exception) {
			if (exception.getStatusCode().is4xxClientError()) {
				throw new CatalogException(exception.getStatusCode().value() == HttpStatus.NOT_FOUND.value()
						? HttpStatus.NOT_FOUND : HttpStatus.FORBIDDEN,
						"An active store profile is required for this operation");
			}
			throw serviceUnavailable();
		}
		catch (ResourceAccessException exception) {
			throw serviceUnavailable();
		}
		catch (RestClientException exception) {
			throw new CatalogException(HttpStatus.BAD_GATEWAY, "Store service returned an invalid response");
		}
	}

	public void requireActiveStore(UUID storeId, String accessToken) {
		try {
			storeRestClient.get().uri("/api/stores/{storeId}", storeId)
					.header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
					.retrieve().toBodilessEntity();
		}
		catch (RestClientResponseException exception) {
			if (exception.getStatusCode().is4xxClientError()) {
				throw new CatalogException(HttpStatus.NOT_FOUND, "Store not found");
			}
			throw serviceUnavailable();
		}
		catch (ResourceAccessException exception) {
			throw serviceUnavailable();
		}
		catch (RestClientException exception) {
			throw new CatalogException(HttpStatus.BAD_GATEWAY, "Store service returned an invalid response");
		}
	}

	private String bearerToken(String accessToken) {
		return "Bearer " + accessToken;
	}

	private CatalogException serviceUnavailable() {
		return new CatalogException(HttpStatus.SERVICE_UNAVAILABLE, "Store service is unavailable");
	}

}
