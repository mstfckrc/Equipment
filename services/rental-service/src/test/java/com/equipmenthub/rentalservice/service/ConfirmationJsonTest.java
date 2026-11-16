package com.equipmenthub.rentalservice.service;

import com.equipmenthub.rentalservice.dto.response.ConfirmationContent;
import org.hibernate.type.format.jackson.Jackson3JsonFormatMapper;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConfirmationJsonTest {

	@Test
	void hibernateMapperPreservesMoneyDatesAndTurkishText() {
		var mapper = new Jackson3JsonFormatMapper(JsonMapper.builder().build());
		ConfirmationContent content = ConfirmationContent.builder().checkoutId(UUID.randomUUID())
				.reservedAt(Instant.now()).preparedAt(Instant.now()).timeZone("Europe/Istanbul")
				.currencyCode("TRY").totalAmount(new BigDecimal("123.45"))
				.stores(List.of(ConfirmationContent.StoreSection.builder().storeId(UUID.randomUUID())
						.storeName("İstanbul Görüntü").items(List.of(ConfirmationContent.Line.builder()
								.pickupAt(OffsetDateTime.parse("2027-02-01T12:00:00+03:00"))
								.dailyPrice(new BigDecimal("123.45")).build())).build())).build();
		String json = mapper.toString(content, ConfirmationContent.class);
		ConfirmationContent restored = mapper.fromString(json, ConfirmationContent.class);
		assertThat(restored.getTotalAmount()).isEqualByComparingTo("123.45");
		assertThat(restored.getStores().getFirst().getStoreName()).isEqualTo("İstanbul Görüntü");
		assertThat(restored.getStores().getFirst().getItems().getFirst().getPickupAt().toInstant())
				.isEqualTo(content.getStores().getFirst().getItems().getFirst().getPickupAt().toInstant());
	}

}
