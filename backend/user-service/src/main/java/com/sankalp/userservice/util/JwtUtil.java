package com.sankalp.userservice.util;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration:86400}")
	private long expiration;

	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes());
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public String extractRole(String token) {
		return extractAllClaims(token).get("role", String.class);
	}

	public String generateToken(String username, String role) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("role", role);
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(username)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	public boolean isTokenValid(String token) {
		try {
			return !extractExpiration(token).before(new Date());
		} catch (Exception ex) {
			return false;
		}
	}

	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
}
