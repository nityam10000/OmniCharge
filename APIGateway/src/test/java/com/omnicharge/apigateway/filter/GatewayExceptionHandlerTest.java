package com.omnicharge.apigateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GatewayExceptionHandlerTest {

    private final GatewayExceptionHandler handler = new GatewayExceptionHandler();

    @Test
    void handle_WithUnauthorizedException_ShouldReturn401() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        UnauthorizedException ex = new UnauthorizedException("Unauthorized test");

        Mono<Void> result = handler.handle(exchange, ex);

        StepVerifier.create(result)
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
        
        // Check if body contains the message
        // This is a bit tricky with DataBuffer, but we can inspect the response
    }

    @Test
    void handle_WithForbiddenException_ShouldReturn403() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        ForbiddenException ex = new ForbiddenException("Forbidden test");

        Mono<Void> result = handler.handle(exchange, ex);

        StepVerifier.create(result)
                .verifyComplete();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void handle_WithGenericException_ShouldReturn500() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        RuntimeException ex = new RuntimeException("Generic error");

        Mono<Void> result = handler.handle(exchange, ex);

        StepVerifier.create(result)
                .verifyComplete();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange.getResponse().getStatusCode());
    }
}
