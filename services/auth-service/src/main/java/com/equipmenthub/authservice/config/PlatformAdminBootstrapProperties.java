package com.equipmenthub.authservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "auth.bootstrap.platform-admin")
public class PlatformAdminBootstrapProperties {

	private String email;
	private String password;

}
