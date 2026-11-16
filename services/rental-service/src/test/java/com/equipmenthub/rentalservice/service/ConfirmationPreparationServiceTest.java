package com.equipmenthub.rentalservice.service;

import com.equipmenthub.rentalservice.client.StoreServiceClient;
import com.equipmenthub.rentalservice.dto.response.ConfirmationContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmationPreparationServiceTest {

	@Mock private ConfirmationPersistenceService persistenceService;
	@Mock private ConfirmationContentService contentService;
	@Mock private StoreServiceClient storeServiceClient;
	@Mock private JsonMapper jsonMapper;
	@InjectMocks private ConfirmationPreparationService service;

	@Test
	void aReadyDocumentDoesNotReadLiveDataOrRegenerate() {
		UUID checkoutId = UUID.randomUUID();
		when(persistenceService.isReady(checkoutId)).thenReturn(true);
		service.prepare(checkoutId);
		verifyNoInteractions(contentService, storeServiceClient, jsonMapper);
		verify(persistenceService, never()).saveReady(any(), any());
	}

	@Test
	void oversizedContentIsNotPersisted() {
		UUID checkoutId = UUID.randomUUID();
		ConfirmationContent content = ConfirmationContent.builder().stores(List.of()).build();
		when(contentService.build(checkoutId)).thenReturn(content);
		when(jsonMapper.writeValueAsBytes(content)).thenReturn(new byte[256 * 1024 + 1]);
		assertThatThrownBy(() -> service.prepare(checkoutId)).isInstanceOf(IllegalArgumentException.class);
		verify(persistenceService, never()).saveReady(any(), any());
	}

}
