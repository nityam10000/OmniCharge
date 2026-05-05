package com.omnicharge.paymentservice.support;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class DefaultAuthenticatedUserContext implements AuthenticatedUserContext {

    @Override
    public String getUserIdHeader() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader("X-User-Id");
        }
        return null;
    }

    @Override
    public String getEmail() {
        return getAuthentication().getName();
    }

    @Override
    public String getRole() {
        return getAuthentication().getAuthorities().iterator().next().getAuthority();
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
