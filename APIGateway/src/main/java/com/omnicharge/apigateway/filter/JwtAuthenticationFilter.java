package com.omnicharge.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.*;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final SecretKey SECRET_KEY =
            Keys.hmacShaKeyFor("mysecretkeymysecretkeymysecretkey123".getBytes(StandardCharsets.UTF_8));

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/auth/login",
            "/auth/send-otp",
            "/auth/verify-otp",
            "/auth/forgot-password",
            "/auth/reset-password",
            "/users/register"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        log.info("Incoming request - Path: {}, Method: {}", path, exchange.getRequest().getMethod());

        boolean isPublic = PUBLIC_ENDPOINTS.stream()
                .anyMatch(path::contains);

        if (isPublic) {
            log.info("Public endpoint accessed - Path: {}, No JWT validation required", path);
            return chain.filter(exchange);
        }

        log.info("Protected endpoint accessed - Path: {}, Validating JWT token", path);

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("JWT validation failed for path: {} - Missing or invalid Authorization header", path);
            return Mono.error(new RuntimeException("Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7);
        log.debug("JWT token extracted from Authorization header for path: {}", path);

        Claims claims;

        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            log.info("JWT token validated successfully for path: {}", path);
        } catch (Exception e) {
            log.error("JWT token validation failed for path: {} - Invalid or expired token", path, e);
            return Mono.error(new RuntimeException("Invalid or expired token"));
        }

        String email = claims.getSubject();
        String role = claims.get("role", String.class);
        Long userId = claims.get("userId", Long.class);

        log.info("JWT claims extracted - Email: {}, Role: {}, UserId: {}, Path: {}", email, role, userId, path);

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Email", email)
                .header("X-User-Role", role)
                .header("X-User-Id", String.valueOf(userId) )
                .build();

        log.info("Request headers mutated with user information - Email: {}, Role: {}, Path: {}", email, role, path);
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }
}