package com.equipmenthub.authservice.client;

import com.equipmenthub.authservice.dto.request.StoreProfileProvisionRequest;
import com.equipmenthub.authservice.dto.response.StoreProfileResponse;
import com.equipmenthub.authservice.exception.AuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClientException;

@Component
public class StoreServiceClient {

	@Autowired
	private RestClient storeServiceRestClient;

	public StoreProfileResponse createProfile(StoreProfileProvisionRequest request) {
		try {
			return storeServiceRestClient.post()
					.uri("/api/stores/internal/profiles")
					.body(request)
					.retrieve()
					.body(StoreProfileResponse.class);
		}
		catch (RestClientResponseException exception) {
			if (exception.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
				throw new AuthException(AuthException.Kind.CONFLICT,
						"This registration could not be completed. Check your details or sign in.");
			}
			throw unavailable();
		}
		catch (ResourceAccessException exception) {
			throw unavailable();
		}
		catch (RestClientException exception) {
			throw new AuthException(AuthException.Kind.SERVICE_UNAVAILABLE,
					"Store setup could not be completed. Retry the same registration shortly.");
		}
	}

	private AuthException unavailable() {
		return new AuthException(AuthException.Kind.SERVICE_UNAVAILABLE,
				"Store setup is temporarily unavailable. Retry the same registration shortly.");
	}

}
