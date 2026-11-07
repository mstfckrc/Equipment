package com.equipmenthub.authservice.service;

import com.equipmenthub.authservice.dto.request.LoginRequest;
import com.equipmenthub.authservice.dto.request.RegisterRequest;
import com.equipmenthub.authservice.dto.response.IssuedAuthSession;
import com.equipmenthub.authservice.dto.response.LoginResponse;
import com.equipmenthub.authservice.dto.response.RegisterResponse;
import com.equipmenthub.authservice.dto.response.RotatedRefreshToken;
import com.equipmenthub.authservice.entity.AuthRole;
import com.equipmenthub.authservice.entity.AuthUser;
import com.equipmenthub.authservice.exception.EmailAlreadyExistsException;
import com.equipmenthub.authservice.exception.InvalidCredentialsException;
import com.equipmenthub.authservice.exception.InvalidRefreshTokenException;
import com.equipmenthub.authservice.exception.InvalidRegistrationException;
import com.equipmenthub.authservice.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

	private static final String TOKEN_TYPE = "Bearer";

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final AuthUserRepository authUserRepository;
	private final PasswordEncoder passwordEncoder;

	public IssuedAuthSession login(LoginRequest request) {
		Authentication authentication;
		try {
			authentication = authenticationManager.authenticate(
					UsernamePasswordAuthenticationToken.unauthenticated(
							request.getEmail().trim(), request.getPassword()));
		}
		catch (InternalAuthenticationServiceException exception) {
			throw exception;
		}
		catch (AuthenticationException exception) {
			throw new InvalidCredentialsException();
		}

		LoginResponse loginResponse = createLoginResponse(jwtService.createAccessToken(authentication));
		RefreshTokenService.CreatedRefreshToken refreshToken = refreshTokenService
				.create(UUID.fromString(authentication.getName()));
		return new IssuedAuthSession(loginResponse, refreshToken.getToken(), refreshToken.getExpiresAt());
	}

	@Transactional
	public RegisterResponse register(RegisterRequest request) {

		String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

		if (authUserRepository.existsByEmailIgnoreCase(email)) {
			throw new EmailAlreadyExistsException();
		}

		if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new InvalidRegistrationException();
		}

		AuthUser user = authUserRepository.save(new AuthUser(
				email, passwordEncoder.encode(request.getPassword()), AuthRole.CUSTOMER));

		return RegisterResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.build();
	}

	public IssuedAuthSession refresh(String refreshToken) {
		RotatedRefreshToken rotated = refreshTokenService.rotate(refreshToken)
				.orElseThrow(InvalidRefreshTokenException::new);
		LoginResponse loginResponse = createLoginResponse(
				jwtService.createAccessToken(rotated.getUserId(), rotated.getRole()));
		return new IssuedAuthSession(loginResponse, rotated.getToken(), rotated.getExpiresAt());
	}

	public void logout(String refreshToken) {
		refreshTokenService.revoke(refreshToken);
	}

	private LoginResponse createLoginResponse(String accessToken) {
		return LoginResponse.builder()
				.accessToken(accessToken)
				.tokenType(TOKEN_TYPE)
				.expiresInSeconds(jwtService.getAccessTokenLifetimeSeconds())
				.build();
	}

}
