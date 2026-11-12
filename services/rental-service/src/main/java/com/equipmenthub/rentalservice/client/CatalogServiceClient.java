package com.equipmenthub.rentalservice.client;

import com.equipmenthub.rentalservice.dto.response.CatalogListingResponse;
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
public class CatalogServiceClient {

	@Autowired
	@Qualifier("catalogRestClient")
	private RestClient catalogRestClient;

	public void requireListingOwnedBy(UUID listingId, UUID storeId, String accessToken) {
		try {
			CatalogListingResponse listing = catalogRestClient.get()
					.uri("/api/catalog/stores/me/products/{listingId}", listingId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
					.retrieve()
					.body(CatalogListingResponse.class);
			if (listing == null || listing.getProductId() == null || listing.getStoreId() == null) {
				throw new RentalException(HttpStatus.BAD_GATEWAY, "Catalog service returned an invalid response");
			}
			if (!listingId.equals(listing.getProductId()) || !storeId.equals(listing.getStoreId())) {
				throw new RentalException(HttpStatus.NOT_FOUND, "Product listing not found");
			}
		}
		catch (RestClientResponseException exception) {
			if (exception.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
				throw new RentalException(HttpStatus.NOT_FOUND, "Product listing not found");
			}
			if (exception.getStatusCode().is4xxClientError()) {
				throw new RentalException(HttpStatus.FORBIDDEN, "The product listing is not available to this store");
			}
			throw serviceUnavailable();
		}
		catch (ResourceAccessException exception) {
			throw serviceUnavailable();
		}
		catch (RestClientException exception) {
			throw new RentalException(HttpStatus.BAD_GATEWAY, "Catalog service returned an invalid response");
		}
	}

	public CatalogListingResponse getPublicListing(UUID storeId, UUID listingId, String accessToken) {
		try {
			CatalogListingResponse listing = catalogRestClient.get()
					.uri("/api/catalog/stores/{storeId}/products/{listingId}", storeId, listingId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
					.retrieve()
					.body(CatalogListingResponse.class);
			if (listing == null || listing.getProductId() == null || listing.getStoreId() == null
					|| listing.getName() == null || listing.getDailyPrice() == null) {
				throw new RentalException(HttpStatus.BAD_GATEWAY, "Catalog service returned an invalid response");
			}
		if (!listingId.equals(listing.getProductId()) || !storeId.equals(listing.getStoreId())) {
			throw new RentalException(HttpStatus.NOT_FOUND, "Product listing not found");
		}
		return listing;
		}
		catch (RestClientResponseException exception) {
			if (exception.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
				throw new RentalException(HttpStatus.NOT_FOUND, "Product listing not found");
			}
			if (exception.getStatusCode().is4xxClientError()) {
				throw new RentalException(HttpStatus.FORBIDDEN, "The product listing is not available");
			}
			throw serviceUnavailable();
		}
		catch (ResourceAccessException exception) {
			throw serviceUnavailable();
		}
		catch (RestClientException exception) {
			throw new RentalException(HttpStatus.BAD_GATEWAY, "Catalog service returned an invalid response");
		}
	}

	private RentalException serviceUnavailable() {
		return new RentalException(HttpStatus.SERVICE_UNAVAILABLE, "Catalog service is unavailable");
	}

}
