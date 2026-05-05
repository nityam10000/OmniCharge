package com.omnicharge.usermanagement.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT Token Validation Filter for UserManagement Service
 * Validates JWT token from Authorization header and sets up SecurityContext
 * Falls back to header-based authentication if available
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    @Value("${jwt.secret:}")
    private String jwtSecret;

    private SecretKey getSecretKey() {
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            log.warn("JWT secret not configured, JWT validation will be skipped");
            return null;
        }
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // First, try to get authentication from JWT token in Authorization header
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                SecretKey secretKey = getSecretKey();
                
                if (secretKey != null) {
                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey(secretKey)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

                    String email = claims.getSubject();
                    String role = claims.get("role", String.class);

                    if (email != null && role != null) {
                        log.debug("JWT token validated successfully for email: {}, role: {}", email, role);

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        email,
                                        null,
                                        List.of(new SimpleGrantedAuthority(role))
                                );

                        SecurityContextHolder.getContext().setAuthentication(auth);
                        filterChain.doFilter(request, response);
                        return;
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to validate JWT token: {}", e.getMessage());
                // Continue to check for header-based authentication
            }
        }

        // Fallback: Try to get authentication from headers (set by API Gateway)
        String email = request.getHeader("X-User-Email");
        String role = request.getHeader("X-User-Role");

        if (email != null && role != null) {
            log.debug("Using header-based authentication for email: {}, role: {}", email, role);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority(role))
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);
        } else if (email != null || role != null) {
            log.warn("Incomplete authentication headers: email={}, role={}", email != null, role != null);
        }

        filterChain.doFilter(request, response);
    }
}
