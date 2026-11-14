package com.equipmenthub.authservice.service;

import com.equipmenthub.authservice.dto.request.AccountProfileUpdateRequest;
import com.equipmenthub.authservice.entity.AuthRole;
import com.equipmenthub.authservice.entity.AuthUser;
import com.equipmenthub.authservice.exception.AuthException;
import com.equipmenthub.authservice.repository.AuthUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountProfileServiceTest {

	@Mock
	private AuthUserRepository users;

	@InjectMocks
	private AuthService service;

	@Test
	void updatesOnlyTheAuthenticatedCustomersName() {
		UUID accountId = UUID.randomUUID();
		AuthUser user = new AuthUser("customer@example.com", "hash", AuthRole.CUSTOMER, "Old", "Name");
		when(users.findById(accountId)).thenReturn(Optional.of(user));
		AccountProfileUpdateRequest request = new AccountProfileUpdateRequest();
		request.setFirstName("  Yeni  ");
		request.setLastName("  İsim  ");

		var result = service.updateOwnProfile(accountId, request);

		assertEquals("Yeni", result.getFirstName());
		assertEquals("İsim", result.getLastName());
		assertEquals("customer@example.com", user.getEmail());
		assertEquals("hash", user.getPasswordHash());
		assertEquals(AuthRole.CUSTOMER, user.getRole());
	}

	@Test
	void rejectsDisabledCustomerEvenWithAnUnexpiredToken() {
		UUID accountId = UUID.randomUUID();
		AuthUser user = new AuthUser("customer@example.com", "hash", AuthRole.CUSTOMER);
		user.setEnabled(false);
		when(users.findById(accountId)).thenReturn(Optional.of(user));

		assertEquals(AuthException.Kind.FORBIDDEN,
				assertThrows(AuthException.class, () -> service.getOwnProfile(accountId)).getKind());
	}

	@Test
	void rejectsStoreOwnerAccountAtServiceBoundary() {
		UUID accountId = UUID.randomUUID();
		AuthUser user = new AuthUser("store@example.com", "hash", AuthRole.STORE_OWNER);
		when(users.findById(accountId)).thenReturn(Optional.of(user));

		assertEquals(AuthException.Kind.FORBIDDEN,
				assertThrows(AuthException.class, () -> service.getOwnProfile(accountId)).getKind());
	}
}
