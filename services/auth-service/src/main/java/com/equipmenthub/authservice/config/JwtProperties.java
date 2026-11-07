package com.equipmenthub.authservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {

	private String secret;
	private String issuer;
	private String audience;
	private long accessTokenLifetimeSeconds = 600;

}
