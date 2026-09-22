package soa.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	@Value("${jwt.secret:BuildAssetLogisticsSecretKeyForJWTTokenGeneration1234567890abcdefghijklmnopqrstuvwxyz}")
	private String secret;

	@Value("${jwt.expiration-ms:86400000}")
	private long expirationMs;

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(String username, String role, String companyName) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("username", username);
		claims.put("role", role);
		claims.put("companyName", companyName);

		return Jwts.builder()
				.claims(claims)
				.subject(username)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + expirationMs))
				.signWith(getSigningKey())
				.compact();
	}

	public Claims validateAndExtractClaims(String token) {
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public String extractUsername(String token) {
		return validateAndExtractClaims(token).getSubject();
	}

	public String extractRole(String token) {
		Object role = validateAndExtractClaims(token).get("role");
		return role != null ? role.toString() : null;
	}

	public boolean isTokenValid(String token) {
		try {
			Claims claims = validateAndExtractClaims(token);
			return !claims.getExpiration().before(new Date());
		} catch (Exception e) {
			return false;
		}
	}
}
