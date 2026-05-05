package com.omnicharge.paymentservice.support;

public interface AuthenticatedUserContext {
    String getUserIdHeader();
    String getEmail();
    String getRole();
}
