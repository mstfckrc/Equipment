package com.equipmenthub.adminservice.client;

import com.equipmenthub.adminservice.dto.request.StoreRegistrationReviewRequest;
import com.equipmenthub.adminservice.dto.response.ApiErrorResponse;
import com.equipmenthub.adminservice.dto.response.StoreRegistrationResponse;
import com.equipmenthub.adminservice.dto.response.StoreRegistrationSummaryResponse;
import com.equipmenthub.adminservice.exception.AdminException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

@Component
public class AuthServiceClient {

	private static final String AUTH_ADMIN_PATH = "/auth/admin/store-registrations";
	private static final String SERVICE_UNAVAILABLE_MESSAGE = "Authentication service is unavailable";

	@Autowired
	@Qualifier("authRestClient")
	private RestClient authRestClient;

	@Autowired
	private JsonMapper jsonMapper;

	public List<StoreRegistrationSummaryResponse> getStoreRegistrations(String accessToken) {
		try {
			return authRestClient.get()
					.uri(AUTH_ADMIN_PATH)
					.header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
					.retrieve()
					.body(new ParameterizedTypeReference<List<StoreRegistrationSummaryResponse>>() { });
		}
		catch (RestClientResponseException exception) {
			throw mapResponseException(exception);
		}
		catch (ResourceAccessException exception) {
			throw serviceUnavailable();
		}
		catch (RestClientException exception) {
			throw invalidDownstreamResponse();
		}
	}

	public StoreRegistrationResponse getStoreRegistration(UUID registrationId, String accessToken) {
		try {
			return authRestClient.get()
					.uri(AUTH_ADMIN_PATH + "/{registrationId}", registrationId)
					.header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
					.retrieve()
					.body(StoreRegistrationResponse.class);
		}
		catch (RestClientResponseException exception) {
			throw mapResponseException(exception);
		}
		catch (ResourceAccessException exception) {
			throw serviceUnavailable();
		}
		catch (RestClientException exception) {
			throw invalidDownstreamResponse();
		}
	}

	public StoreRegistrationResponse reviewStoreRegistration(
			UUID registrationId, StoreRegistrationReviewRequest request, String accessToken) {
		try {
			return authRestClient.method(HttpMethod.PATCH)
					.uri(AUTH_ADMIN_PATH + "/{registrationId}", registrationId)
					.header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
					.body(request)
					.retrieve()
					.body(StoreRegistrationResponse.class);
		}
		catch (RestClientResponseException exception) {
			throw mapResponseException(exception);
		}
		catch (ResourceAccessException exception) {
			throw serviceUnavailable();
		}
		catch (RestClientException exception) {
			throw invalidDownstreamResponse();
		}
	}

	private String bearerToken(String accessToken) {
		return "Bearer " + accessToken;
	}

	private AdminException mapResponseException(RestClientResponseException exception) {
		HttpStatusCode status = exception.getStatusCode();
		if (status.is4xxClientError()) {
			return new AdminException(status, readSafeMessage(exception));
		}
		return serviceUnavailable();
	}

	private String readSafeMessage(RestClientResponseException exception) {
		try {
			ApiErrorResponse error = jsonMapper.readValue(
					exception.getResponseBodyAsByteArray(), ApiErrorResponse.class);
			if (error.getMessage() != null && !error.getMessage().isBlank()) {
				return error.getMessage();
			}
		}
		catch (JacksonException ignored) {
			// Do not expose an unstructured downstream response body.
		}
		return "Request rejected by authentication service";
	}

	private AdminException serviceUnavailable() {
		return new AdminException(HttpStatus.SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_MESSAGE);
	}

	private AdminException invalidDownstreamResponse() {
		return new AdminException(HttpStatus.BAD_GATEWAY, "Authentication service returned an invalid response");
	}

}
