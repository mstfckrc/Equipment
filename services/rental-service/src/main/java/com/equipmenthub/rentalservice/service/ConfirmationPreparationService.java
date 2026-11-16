package com.equipmenthub.rentalservice.service;

import com.equipmenthub.rentalservice.client.StoreServiceClient;
import com.equipmenthub.rentalservice.dto.response.ConfirmationContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Service
public class ConfirmationPreparationService {

	@Autowired
	private ConfirmationPersistenceService persistenceService;

	@Autowired
	private ConfirmationContentService contentService;

	@Autowired
	private StoreServiceClient storeServiceClient;

	@Autowired
	private JsonMapper jsonMapper;

	public void prepare(UUID checkoutId) {
		if (persistenceService.isReady(checkoutId)) {
			return;
		}
		ConfirmationContent content = contentService.build(checkoutId);
		// External calls run outside DB transactions and locks. Names are fixed at preparation time.
		for (ConfirmationContent.StoreSection store : content.getStores()) {
			store.setStoreName(storeServiceClient.getPublicStoreName(store.getStoreId()));
		}
		if (jsonMapper.writeValueAsBytes(content).length > 256 * 1024) {
			throw new IllegalArgumentException("Confirmation content exceeds size limit");
		}
		persistenceService.saveReady(checkoutId, content);
	}

}
