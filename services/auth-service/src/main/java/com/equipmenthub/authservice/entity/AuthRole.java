package com.equipmenthub.authservice.entity;

public enum AuthRole {

	CUSTOMER,
	STORE_OWNER,
	PLATFORM_ADMIN;

	public String getAuthority() {
		return "ROLE_" + name();
	}

}
