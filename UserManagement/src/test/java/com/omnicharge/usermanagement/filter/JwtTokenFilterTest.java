package com.omnicharge.usermanagement.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenFilterTest {

    private JwtTokenFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private static final String SECRET = "aVeryLongSecretKeyForTestingJwtTokenFilterWhichMustBeAtLeast32Bytes";

    @BeforeEach
    void setUp() {
        filter = new JwtTokenFilter();
        ReflectionTestUtils.setField(filter, "jwtSecret", SECRET);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithValidJwt_ShouldAuthenticate() throws ServletException, IOException {
        String token = Jwts.builder()
                .setSubject("test@example.com")
                .claim("role", "ROLE_USER")
                .setExpiration(new Date(System.currentTimeMillis() + 10000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("test@example.com", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithInvalidJwt_ShouldFallbackToHeaders() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(request.getHeader("X-User-Email")).thenReturn("header@example.com");
        when(request.getHeader("X-User-Role")).thenReturn("ROLE_ADMIN");

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("header@example.com", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithMissingAuthHeader_ShouldFallbackToHeaders() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getHeader("X-User-Email")).thenReturn("header@example.com");
        when(request.getHeader("X-User-Role")).thenReturn("ROLE_ADMIN");

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("header@example.com", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithNoAuth_ShouldNotAuthenticate() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getHeader("X-User-Email")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithNoSecretKey_ShouldNotAttemptJwtValidation() throws ServletException, IOException {
        ReflectionTestUtils.setField(filter, "jwtSecret", "");
        
        String token = "any-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
