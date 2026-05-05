package com.omnicharge.apigateway.filter;

import com.omnicharge.apigateway.filter.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/auth/login",
            "/auth/send-otp",
            "/auth/verify-otp",
            "/auth/refresh-token",
            "/auth/forgot-password",
            "/auth/reset-password",
            "/users/register",
            "/users/paginated",
            "/operators/getList",
            "/operators/getList/paginated",
            "/plans",
            "/plans/paginated"
    );

    private static final List<String> PROTECTED_ENDPOINTS = List.of(
            "/plans/create",
            "/plans/update",
            "/plans/delete",
            "/operators/register",
            "/operators/update",
            "/operators/delete"
    );

    private static final List<String> ACTUATOR_ENDPOINT_PREFIXES = List.of(
            "/auth/actuator",
            "/users/actuator",
            "/recharge/actuator",
            "/transaction/actuator",
            "/operators/actuator",
            "/plans/actuator",
            "/notifications/actuator",
            "/auth/metrics",
            "/users/metrics",
            "/recharge/metrics",
            "/transaction/metrics",
            "/operators/metrics",
            "/plans/metrics",
            "/notifications/metrics"
    );

    private boolean isPublicPath(String path) {
        // Protected endpoints take precedence - don't bypass JWT for them
        boolean isProtected = PROTECTED_ENDPOINTS.stream().anyMatch(protectedPath ->
                path.equals(protectedPath) || path.startsWith(protectedPath + "/"));
        if (isProtected) {
            return false;
        }
        
        // Check if it's a public endpoint
        return PUBLIC_ENDPOINTS.stream().anyMatch(publicPath ->
                path.equals(publicPath) || path.startsWith(publicPath + "/"));
    }

    private boolean isActuatorPath(String path) {
        return ACTUATOR_ENDPOINT_PREFIXES.stream().anyMatch(prefix ->
                path.equals(prefix) || path.startsWith(prefix + "/"));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        log.info("Incoming request - Path: {}, Method: {}", path, exchange.getRequest().getMethod());


        boolean isPublic = isPublicPath(path);
        boolean isActuator = isActuatorPath(path);

        if (isPublic || isActuator) {
            log.info("Bypassing JWT validation - Path: {}", path);
            return chain.filter(exchange);
        }

        log.info("Protected endpoint accessed - Path: {}, Validating JWT token", path);

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("JWT validation failed for path: {} - Missing or invalid Authorization header", path);
            return Mono.error(new UnauthorizedException("Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7);
        log.debug("JWT token extracted from Authorization header for path: {}", path);

        Claims claims;

        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("JWT validation failed for path: {} - {}", path, e.getMessage());
            return Mono.error(new UnauthorizedException("Invalid or expired token"));
        }

        log.info("JWT token validated successfully for path: {}", path);

        String email = claims.getSubject();  // Email is stored as the subject claim
        String role = claims.get("role", String.class);
        Long userId = claims.get("userId", Long.class);

        log.info("JWT claims extracted - Email: {}, Role: {}, UserId: {}, Path: {}", email, role, userId, path);

        if (email == null || role == null || userId == null) {
            log.error("Missing required JWT claims - Email: {}, Role: {}, UserId: {}", email, role, userId);
            return Mono.error(new UnauthorizedException("Invalid JWT token: missing required claims"));
        }

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Email", email)
                .header("X-User-Role", role)
                .header("X-User-Id", String.valueOf(userId))
                .build();

        log.info("Request headers mutated with user information - Email: {}, Role: {}, Path: {}", email, role, path);

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
