package com.omnicharge.rechargeprocessing.exception;

import com.omnicharge.rechargeprocessing.dto.RechargeRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/recharge/test");
    }

    @Test
    void handleRechargeNotFoundException_ShouldReturnNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handleRechargeNotFoundException(
                new RechargeNotFoundException("missing"), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("missing", response.getBody().getMessage());
    }

    @Test
    void handleUserNotRegisteredException_ShouldReturnNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handleUserNotRegisteredException(
                new UserNotRegisteredException("unknown"), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("unknown", response.getBody().getMessage());
    }

    @Test
    void handleMethodArgumentNotValidException_ShouldReturnBadRequest() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new RechargeRequestDTO(), "dto");
        bindingResult.addError(new FieldError("dto", "planId", "must not be null"));
        MethodParameter parameter = new MethodParameter(
                DummyMethods.class.getDeclaredMethod("accept", RechargeRequestDTO.class), 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValidException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("/recharge/test", response.getBody().getPath());
    }

    @Test
    void handleException_ShouldReturnInternalServerError() {
        ResponseEntity<ErrorResponse> response = handler.handleException(new RuntimeException("boom"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("boom", response.getBody().getMessage());
    }

    @Test
    void handleOperatorPlanMismachedException_ShouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleOperatorPlanMismachedException(
                new OperatorPlanMismachedException("mismatch"), request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("mismatch", response.getBody().getMessage());
    }

    @Test
    void handleServiceUnavailableException_ShouldReturnServiceUnavailable() {
        ResponseEntity<ErrorResponse> response = handler.handleServiceUnavailableException(
                new ServiceUnavailableException("down"), request);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("down", response.getBody().getMessage());
    }

    private static class DummyMethods {
        @SuppressWarnings("unused")
        public void accept(RechargeRequestDTO dto) {
        }
    }
}
