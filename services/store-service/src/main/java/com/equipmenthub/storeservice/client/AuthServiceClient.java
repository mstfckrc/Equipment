package com.equipmenthub.storeservice.client;

import com.equipmenthub.storeservice.exception.StoreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.UUID;

@Component
public class AuthServiceClient {

	@Autowired
	@Qualifier("authRestClient")
	private RestClient authRestClient;

	public List<UUID> getApprovedStoreIds(String accessToken) {
		try {
			List<UUID> storeIds = authRestClient.get().uri("/auth/internal/approved-store-ids")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
					.retrieve().body(new ParameterizedTypeReference<List<UUID>>() { });
			if (storeIds == null) {
				throw new StoreException(HttpStatus.BAD_GATEWAY, "Authentication service returned an invalid response");
			}
			return storeIds;
		}
		catch (RestClientResponseException exception) {
			if (exception.getStatusCode().is4xxClientError()) {
				throw new StoreException(HttpStatus.BAD_GATEWAY, "Authentication service rejected the store lookup");
			}
			throw serviceUnavailable();
		}
		catch (ResourceAccessException exception) {
			throw serviceUnavailable();
		}
		catch (RestClientException exception) {
			throw new StoreException(HttpStatus.BAD_GATEWAY, "Authentication service returned an invalid response");
		}
	}

	private StoreException serviceUnavailable() {
		return new StoreException(HttpStatus.SERVICE_UNAVAILABLE, "Store discovery is temporarily unavailable");
	}

}
