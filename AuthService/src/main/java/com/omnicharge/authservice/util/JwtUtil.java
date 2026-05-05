package com.omnicharge.authservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration-ms:3600000}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String email, String role, Long userId) {
        log.info("Generating access token for email: {}, role: {}, userId: {}", email, role, userId);
        return buildToken(email, role, userId, accessTokenExpirationMs, "access");
    }

    public String generateRefreshToken(String email, String role, Long userId) {
        log.info("Generating refresh token for email: {}, role: {}, userId: {}", email, role, userId);
        return buildToken(email, role, userId, refreshTokenExpirationMs, "refresh");
    }

    private String buildToken(String email, String role, Long userId, long expirationMs, String tokenType) {
        try {
            String token = Jwts.builder()
                    .setSubject(email)
                    .claim("role", "ROLE_" + role)
                    .claim("userId", userId)
                    .claim("tokenType", tokenType)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                    .signWith(getSecretKey())
                    .compact();

            log.info("{} token generated successfully for email: {}", tokenType, email);
            return token;
        } catch (Exception e) {
            log.error("Failed to generate {} token for email: {}", tokenType, email, e);
            throw e;
        }
    }

    public Claims extractClaims(String token) {
        log.debug("Extracting claims from JWT token");
        
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token).getBody();
            
            log.info("Claims extracted successfully for subject: {}", claims.getSubject());
            return claims;
        } catch (Exception e) {
            log.error("Failed to extract claims from JWT token", e);
            throw e;
        }
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractTokenType(String token) {
        return extractClaims(token).get("tokenType", String.class);
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }
}
