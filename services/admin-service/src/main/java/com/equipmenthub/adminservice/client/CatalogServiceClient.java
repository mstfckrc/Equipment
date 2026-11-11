package com.equipmenthub.adminservice.client;

import com.equipmenthub.adminservice.dto.request.ProductModerationRequest;
import com.equipmenthub.adminservice.dto.response.AdminProductResponse;
import com.equipmenthub.adminservice.dto.response.ApiErrorResponse;
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
public class CatalogServiceClient {

	private static final String CATALOG_ADMIN_PATH = "/internal/admin/products";

	@Autowired
	@Qualifier("catalogRestClient")
	private RestClient catalogRestClient;

	@Autowired
	private JsonMapper jsonMapper;

	public List<AdminProductResponse> getProducts(String accessToken) {
		try {
			return catalogRestClient.get().uri(CATALOG_ADMIN_PATH)
					.header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
					.retrieve().body(new ParameterizedTypeReference<List<AdminProductResponse>>() { });
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

	public AdminProductResponse getProduct(UUID productId, String accessToken) {
		try {
			return catalogRestClient.get().uri(CATALOG_ADMIN_PATH + "/{productId}", productId)
					.header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
					.retrieve().body(AdminProductResponse.class);
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

	public AdminProductResponse updateStatus(
			UUID productId, ProductModerationRequest request, String accessToken) {
		try {
			return catalogRestClient.method(HttpMethod.PATCH).uri(CATALOG_ADMIN_PATH + "/{productId}", productId)
					.header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)).body(request)
					.retrieve().body(AdminProductResponse.class);
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
			return new AdminException(status, "Request rejected by catalog service");
		}
		return serviceUnavailable();
	}

	private AdminException serviceUnavailable() {
		return new AdminException(HttpStatus.SERVICE_UNAVAILABLE, "Catalog service is unavailable");
	}

	private AdminException invalidDownstreamResponse() {
		return new AdminException(HttpStatus.BAD_GATEWAY, "Catalog service returned an invalid response");
	}

}
