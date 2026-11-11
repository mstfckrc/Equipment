package com.equipmenthub.adminservice.client;

import com.equipmenthub.adminservice.dto.request.StoreStatusUpdateRequest;
import com.equipmenthub.adminservice.dto.response.ApiErrorResponse;
import com.equipmenthub.adminservice.dto.response.StoreProfileResponse;
import com.equipmenthub.adminservice.dto.response.StoreSummaryResponse;
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
public class StoreServiceClient {

	private static final String STORE_ADMIN_PATH = "/api/stores/admin";
	private static final String SERVICE_UNAVAILABLE_MESSAGE = "Store service is unavailable";

	@Autowired
	@Qualifier("storeRestClient")
	private RestClient storeRestClient;

	@Autowired
	private JsonMapper jsonMapper;

	public List<StoreSummaryResponse> getStores(String accessToken) {
		try {
			return storeRestClient.get().uri(STORE_ADMIN_PATH)
					.header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
					.retrieve().body(new ParameterizedTypeReference<List<StoreSummaryResponse>>() { });
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

	public StoreProfileResponse getStore(UUID storeId, String accessToken) {
		try {
			return storeRestClient.get().uri(STORE_ADMIN_PATH + "/{storeId}", storeId)
					.header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
					.retrieve().body(StoreProfileResponse.class);
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

	public StoreProfileResponse updateStoreStatus(
			UUID storeId, StoreStatusUpdateRequest request, String accessToken) {
		try {
			return storeRestClient.method(HttpMethod.PATCH)
					.uri(STORE_ADMIN_PATH + "/{storeId}", storeId)
					.header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
					.body(request)
					.retrieve().body(StoreProfileResponse.class);
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
			try {
				ApiErrorResponse error = jsonMapper.readValue(
						exception.getResponseBodyAsByteArray(), ApiErrorResponse.class);
				if (error.getMessage() != null && !error.getMessage().isBlank()) {
					return new AdminException(status, error.getMessage());
				}
			}
			catch (JacksonException ignored) {
				// Do not expose an unstructured downstream response body.
			}
			return new AdminException(status, "Request rejected by store service");
		}
		return serviceUnavailable();
	}

	private AdminException serviceUnavailable() {
		return new AdminException(HttpStatus.SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_MESSAGE);
	}

	private AdminException invalidDownstreamResponse() {
		return new AdminException(HttpStatus.BAD_GATEWAY, "Store service returned an invalid response");
	}

}
