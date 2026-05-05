package com.omnicharge.authservice.service;

import com.omnicharge.authservice.dto.NotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private OtpService otpService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void sendOtp_ShouldStoreInRedisAndPublishEvent() {
        String email = "test@test.com";
        
        otpService.sendOtp(email);

        verify(valueOperations).set(eq("OTP:LOGIN:" + email), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(NotificationEvent.class));
    }

    @Test
    void verifyOtp_WithCorrectOtp_ShouldReturnTrueAndDelete() {
        String email = "test@test.com";
        String otp = "123456";
        when(valueOperations.get("OTP:LOGIN:" + email)).thenReturn(otp);

        boolean result = otpService.verifyOtp(email, otp);

        assertTrue(result);
        verify(redisTemplate).delete("OTP:LOGIN:" + email);
    }

    @Test
    void verifyOtp_WithIncorrectOtp_ShouldReturnFalse() {
        String email = "test@test.com";
        String otp = "123456";
        when(valueOperations.get("OTP:LOGIN:" + email)).thenReturn("wrong");

        boolean result = otpService.verifyOtp(email, otp);

        assertFalse(result);
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void sendForgotPasswordOtp_ShouldStoreInRedisAndPublishEvent() {
        String email = "test@test.com";
        
        otpService.sendForgotPasswordOtp(email);

        verify(valueOperations).set(eq("OTP:FORGOT:" + email), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(NotificationEvent.class));
    }

    @Test
    void verifyForgotPasswordOtp_WithCorrectOtp_ShouldReturnTrueAndDelete() {
        String email = "test@test.com";
        String otp = "123456";
        when(valueOperations.get("OTP:FORGOT:" + email)).thenReturn(otp);

        boolean result = otpService.verifyForgotPasswordOtp(email, otp);

        assertTrue(result);
        verify(redisTemplate).delete("OTP:FORGOT:" + email);
    }
}
