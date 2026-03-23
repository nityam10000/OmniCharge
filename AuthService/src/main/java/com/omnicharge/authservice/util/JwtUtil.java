package com.omnicharge.authservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@Slf4j
public class JwtUtil {

    private static final SecretKey SECRET_KEY =
            Keys.hmacShaKeyFor("mysecretkeymysecretkeymysecretkey123".getBytes());

    public String generateToken(String email, String role,  Long userId) {
        log.info("Generating JWT token for email: {}, role: {}, userId: {}", email, role, userId);
        
        try {
            String token = Jwts.builder()
                    .setSubject(email)
                    .claim("role", "ROLE_" + role)
                    .claim("userId", userId)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + 180000000))
                    .signWith(SECRET_KEY)
                    .compact();
            
            log.info("JWT token generated successfully for email: {}", email);
            return token;
        } catch (Exception e) {
            log.error("Failed to generate JWT token for email: {}", email, e);
            throw e;
        }
    }

    public Claims extractClaims(String token) {
        log.debug("Extracting claims from JWT token");
        
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token).getBody();
            
            log.info("Claims extracted successfully for subject: {}", claims.getSubject());
            return claims;
        } catch (Exception e) {
            log.error("Failed to extract claims from JWT token", e);
            throw e;
        }
    }
}