package com.omnicharge.rechargeprocessing.support;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultRequestUserContextTest {

    @InjectMocks
    private DefaultRequestUserContext userContext;

    @Mock
    private ServletRequestAttributes attributes;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        RequestContextHolder.resetRequestAttributes();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getUserIdHeader_WithAttributes_ShouldReturnHeader() {
        RequestContextHolder.setRequestAttributes(attributes);
        when(attributes.getRequest()).thenReturn(request);
        when(request.getHeader("X-User-Id")).thenReturn("test-user-id");

        String result = userContext.getUserIdHeader();

        assertEquals("test-user-id", result);
    }

    @Test
    void getUserIdHeader_WithNullAttributes_ShouldReturnNull() {
        RequestContextHolder.setRequestAttributes(null);

        String result = userContext.getUserIdHeader();

        assertNull(result);
    }
}
