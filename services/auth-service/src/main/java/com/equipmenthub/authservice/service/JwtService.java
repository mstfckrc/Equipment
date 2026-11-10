package com.equipmenthub.authservice.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.equipmenthub.authservice.config.JwtProperties;
import com.equipmenthub.authservice.entity.AuthRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

	private final JwtEncoder jwtEncoder;
	private final JwtProperties jwtProperties;

	public String createAccessToken(Authentication authentication) {
		return createAccessToken(authentication.getName(), authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.toList());
	}

	public String createAccessToken(UUID userId, AuthRole role) {
		return createAccessToken(userId.toString(), List.of(role.getAuthority()));
	}

	private String createAccessToken(String subject, List<String> roles) {
		Instant issuedAt = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(jwtProperties.getIssuer())
				.subject(subject)
				.audience(List.of(jwtProperties.getAudience()))
				.issuedAt(issuedAt)
				.expiresAt(issuedAt.plusSeconds(jwtProperties.getAccessTokenLifetimeSeconds()))
				.id(UUID.randomUUID().toString())
				.claim("roles", roles)
				.build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

	public long getAccessTokenLifetimeSeconds() {
		return jwtProperties.getAccessTokenLifetimeSeconds();
	}

}
