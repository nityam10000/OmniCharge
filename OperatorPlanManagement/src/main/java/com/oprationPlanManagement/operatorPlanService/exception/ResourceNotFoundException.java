package com.oprationPlanManagement.operatorPlanService.exception;

public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L; // for serialization safety

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}