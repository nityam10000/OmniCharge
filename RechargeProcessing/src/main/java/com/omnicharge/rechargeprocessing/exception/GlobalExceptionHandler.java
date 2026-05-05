package com.omnicharge.rechargeprocessing.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RechargeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRechargeNotFoundException(
            RechargeNotFoundException e, HttpServletRequest request) {
        log.warn("Recharge not found: {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(UserNotRegisteredException.class)
    public ResponseEntity<ErrorResponse> handleUserNotRegisteredException(
            UserNotRegisteredException e, HttpServletRequest request) {
        log.warn("User not registered: {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OperatorPlanMismachedException.class)
    public ResponseEntity<ErrorResponse> handleOperatorPlanMismachedException(
            OperatorPlanMismachedException e, HttpServletRequest request) {
        log.warn("Operator plan mismatch: {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailableException(
            ServiceUnavailableException e, HttpServletRequest request) {
        log.error("Service unavailable: {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("Validation error: {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.error("Access Denied", e);
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e, HttpServletRequest request) {
        log.error("Unexpected error", e);
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage() != null ? e.getMessage() : "Internal Server Error", 
                HttpStatus.INTERNAL_SERVER_ERROR, 
                request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}