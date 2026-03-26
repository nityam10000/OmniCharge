package com.omnicharge.rechargeprocessing.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@RequiredArgsConstructor
public class ErrorResponse {

    private String message;
    private HttpStatus status;
    private LocalDateTime timestamp;
    private String path;
    
    public ErrorResponse(String message, HttpStatus status, String path) {
        this.message = message;
        this.status = status;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}