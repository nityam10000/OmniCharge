package com.omnicharge.authservice;

import com.omnicharge.authservice.exception.ErrorResponse;
import com.omnicharge.authservice.exception.GlobalExceptionHandler;
import com.omnicharge.authservice.exception.UserAlreadyExistsException;
import com.omnicharge.authservice.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final HttpServletRequest request = mock(HttpServletRequest.class);

    @Test
    void handleUserNotFound_Returns404() {
        when(request.getRequestURI()).thenReturn("/auth/login");
        ResponseEntity<ErrorResponse> response =
                handler.handleUserNotFoundException(new UserNotFoundException("Not found"), request);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void handleUserAlreadyExists_Returns400() {
        when(request.getRequestURI()).thenReturn("/auth/register");
        ResponseEntity<ErrorResponse> response =
                handler.handleUserAlreadyExistsException(new UserAlreadyExistsException("Exists"), request);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void handleGenericException_Returns400() {
        when(request.getRequestURI()).thenReturn("/auth/login");
        ResponseEntity<ErrorResponse> response =
                handler.handleException(new RuntimeException("Error"), request);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void handleValidation_Returns400() {
        when(request.getRequestURI()).thenReturn("/auth/login");
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getMessage()).thenReturn("Validation failed");

        ResponseEntity<ErrorResponse> response =
                handler.handleMethodArgumentNotValidException(ex, request);
        assertEquals(400, response.getStatusCode().value());
    }
}