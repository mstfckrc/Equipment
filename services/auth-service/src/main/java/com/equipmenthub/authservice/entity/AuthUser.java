package com.equipmenthub.authservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "auth_user")
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class AuthUser {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, unique = true, length = 254)
	private String email;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private AuthRole role;

	@Column(nullable = false)
	private boolean enabled = true;

	@Enumerated(EnumType.STRING)
	@Column(name = "account_status", length = 16)
	@Getter(AccessLevel.NONE)
	private AccountStatus accountStatus = AccountStatus.ACTIVE;

	@Column(name = "first_name", length = 100)
	private String firstName;

	@Column(name = "last_name", length = 100)
	private String lastName;

	public AuthUser(String email, String passwordHash, AuthRole role) {
		this(email, passwordHash, role, null, null);
	}

	public AuthUser(String email, String passwordHash, AuthRole role, String firstName, String lastName) {
		this.email = normalizeEmail(email);
		this.passwordHash = passwordHash;
		this.role = role;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public AccountStatus getAccountStatus() {
		if (accountStatus != null) {
			return accountStatus;
		}
		// Rows created before account review was introduced remain usable.
		return AccountStatus.ACTIVE;
	}

	@PrePersist
	@PreUpdate
	private void normalizeEmail() {
		this.email = normalizeEmail(this.email);
	}

	private static String normalizeEmail(String email) {
		if (email == null) {
			return null;
		}
		return email.trim().toLowerCase(Locale.ROOT);
	}

}
