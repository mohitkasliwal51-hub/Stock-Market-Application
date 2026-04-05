package com.sankalp.userservice.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sankalp.userservice.dto.AuthResponse;
import com.sankalp.userservice.dto.LoginRequest;
import com.sankalp.userservice.dto.RegisterRequest;
import com.sankalp.userservice.dto.UserProfileDto;
import com.sankalp.userservice.entity.UserAccount;
import com.sankalp.userservice.entity.UserRole;
import com.sankalp.userservice.repository.UserAccountRepository;
import com.sankalp.userservice.util.JwtUtil;

@Service
public class AuthService {

	private final UserAccountRepository userAccountRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	public AuthService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
		this.userAccountRepository = userAccountRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtil = jwtUtil;
	}

	public AuthResponse register(RegisterRequest request) {
		if (userAccountRepository.existsByUsername(request.getUsername())) {
			throw new IllegalArgumentException("Username already exists");
		}
		if (userAccountRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("Email already exists");
		}

		UserAccount user = new UserAccount();
		user.setUsername(request.getUsername());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(request.getRole() == null ? UserRole.INVESTOR : request.getRole());
		UserAccount saved = userAccountRepository.save(user);

		String token = jwtUtil.generateToken(saved.getUsername(), saved.getRole().name());
		return new AuthResponse(token, saved.getUsername(), saved.getRole().name());
	}

	public AuthResponse login(LoginRequest request) {
		Optional<UserAccount> userOptional = userAccountRepository.findByUsername(request.getUsername());
		if (userOptional.isEmpty()) {
			throw new IllegalArgumentException("Invalid username or password");
		}

		UserAccount user = userOptional.get();
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new IllegalArgumentException("Invalid username or password");
		}

		String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
		return new AuthResponse(token, user.getUsername(), user.getRole().name());
	}

	public UserProfileDto getProfileFromToken(String token) {
		if (!jwtUtil.isTokenValid(token)) {
			throw new IllegalArgumentException("Invalid or expired token");
		}

		String username = jwtUtil.extractUsername(token);
		UserAccount user = userAccountRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		return new UserProfileDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
	}
}
