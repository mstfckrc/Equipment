package com.equipmenthub.authservice.entity;

public enum AuthRole {

	CUSTOMER,
	STORE_OWNER,
	/**
	 * Legacy database value; it receives the same authority as STORE_OWNER and is
	 * never assigned to new accounts.
	 */
	@Deprecated
	STORE_ADMIN,
	PLATFORM_ADMIN;

	public String getAuthority() {
		return "ROLE_" + (this == STORE_ADMIN ? STORE_OWNER.name() : name());
	}

}
