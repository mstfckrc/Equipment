package com.equipmenthub.authservice.service;

import com.equipmenthub.authservice.entity.AuthUser;
import com.equipmenthub.authservice.repository.AuthUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthUserDetailsService implements UserDetailsService {

	@Autowired
	private AuthUserRepository authUserRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		AuthUser authUser = authUserRepository.findByEmailIgnoreCase(email.trim())
				.orElseThrow(() -> new UsernameNotFoundException("User was not found"));

		return User.withUsername(authUser.getId().toString())
				.password(authUser.getPasswordHash())
				.authorities(authUser.getRole().getAuthority())
				.disabled(!authUser.isEnabled())
				.build();
	}

}
