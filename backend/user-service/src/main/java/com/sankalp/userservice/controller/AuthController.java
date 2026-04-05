package com.sankalp.userservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sankalp.userservice.dto.ApiResult;
import com.sankalp.userservice.dto.AuthResponse;
import com.sankalp.userservice.dto.LoginRequest;
import com.sankalp.userservice.dto.RegisterRequest;
import com.sankalp.userservice.dto.UserProfileDto;
import com.sankalp.userservice.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public ResponseEntity<ApiResult<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
		try {
			AuthResponse response = authService.register(request);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(ApiResult.success("User registered successfully", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(ApiResult.error(ex.getMessage(), "AUTH_CONFLICT"));
		}
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResult<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
		try {
			AuthResponse response = authService.login(request);
			return ResponseEntity.ok(ApiResult.success("Login successful", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResult.error(ex.getMessage(), "AUTH_INVALID_CREDENTIALS"));
		}
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResult<UserProfileDto>> me(@RequestHeader("Authorization") String authorization) {
		try {
			String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
			UserProfileDto profile = authService.getProfileFromToken(token);
			return ResponseEntity.ok(ApiResult.success("Profile retrieved successfully", profile));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResult.error(ex.getMessage(), "AUTH_INVALID_TOKEN"));
		}
	}
}
