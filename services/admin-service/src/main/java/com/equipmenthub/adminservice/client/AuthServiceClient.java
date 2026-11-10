package com.equipmenthub.adminservice.client;

import com.equipmenthub.adminservice.dto.request.StoreRegistrationReviewRequest;
import com.equipmenthub.adminservice.dto.response.ApiErrorResponse;
import com.equipmenthub.adminservice.dto.response.StoreRegistrationResponse;
import com.equipmenthub.adminservice.exception.AdminException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
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
@RequiredArgsConstructor
public class AuthServiceClient {

	private static final String AUTH_ADMIN_PATH = "/auth/admin/store-registrations";
	private static final String SERVICE_UNAVAILABLE_MESSAGE = "Authentication service is unavailable";

	private final RestClient authRestClient;
	private final JsonMapper jsonMapper;

	public List<StoreRegistrationResponse> getPendingStoreRegistrations(String accessToken) {
		try {
			return authRestClient.get()
					.uri(AUTH_ADMIN_PATH)
					.header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
					.retrieve()
					.body(new ParameterizedTypeReference<>() { });
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

	public StoreRegistrationResponse approveStoreRegistration(UUID registrationId, String accessToken) {
		try {
			return authRestClient.post()
					.uri(AUTH_ADMIN_PATH + "/{registrationId}/approve", registrationId)
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

	public StoreRegistrationResponse rejectStoreRegistration(
			UUID registrationId, StoreRegistrationReviewRequest request, String accessToken) {
		try {
			return authRestClient.post()
					.uri(AUTH_ADMIN_PATH + "/{registrationId}/reject", registrationId)
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
