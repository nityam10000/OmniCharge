package com.omnicharge.authservice.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secret = "my-secret-key-that-is-at-least-32-characters-long-for-hmac-sha";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpirationMs", 3600000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpirationMs", 604800000L);
    }

    @Test
    void generateAndExtractTokens_ShouldWorkCorrectly() {
        String email = "test@test.com";
        String role = "USER";
        Long userId = 1L;

        String accessToken = jwtUtil.generateAccessToken(email, role, userId);
        assertNotNull(accessToken);

        Claims claims = jwtUtil.extractClaims(accessToken);
        assertEquals(email, claims.getSubject());
        assertEquals("ROLE_USER", claims.get("role"));
        assertEquals(userId, claims.get("userId", Long.class));
        assertEquals("access", claims.get("tokenType"));

        String refreshToken = jwtUtil.generateRefreshToken(email, role, userId);
        assertNotNull(refreshToken);
        assertEquals("refresh", jwtUtil.extractTokenType(refreshToken));
        assertEquals(email, jwtUtil.extractEmail(refreshToken));
    }

    @Test
    void getRefreshTokenExpirationMs_ShouldReturnConfiguredValue() {
        assertEquals(604800000L, jwtUtil.getRefreshTokenExpirationMs());
    }
}
