package com.oprationPlanManagement.operatorPlanService.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFound_Returns404() {
        ResponseEntity<Map<String, String>> res =
                handler.handleResourceNotFound(new ResourceNotFoundException("not found"));
        assertEquals(404, res.getStatusCode().value());
        assertTrue(res.getBody().containsKey("error"));
    }

    @Test
    void handleGlobalException_Returns500() {
        ResponseEntity<Map<String, String>> res =
                handler.handleGlobalException(new RuntimeException("unexpected"));
        assertEquals(500, res.getStatusCode().value());
    }

    @Test
    void handleValidation_Returns400WithFieldErrors() {
        BindingResult br = mock(BindingResult.class);
        when(br.getFieldErrors()).thenReturn(
                List.of(new FieldError("obj", "name", "required")));
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, br);

        ResponseEntity<Map<String, String>> res = handler.handleValidationExceptions(ex);
        assertEquals(400, res.getStatusCode().value());
        assertTrue(res.getBody().containsKey("name"));
    }
}