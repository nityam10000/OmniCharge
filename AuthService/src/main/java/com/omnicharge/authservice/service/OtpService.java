package com.omnicharge.authservice.service;

import com.omnicharge.authservice.config.RabbitMQConfig;
import com.omnicharge.authservice.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    private static final long OTP_TTL_MINUTES = 5;

    // ── Redis key prefixes ─────────────────────────────────────────────────
    private static final String OTP_LOGIN_PREFIX  = "OTP:LOGIN:";
    private static final String OTP_FORGOT_PREFIX = "OTP:FORGOT:";

    // ══════════════════════════════════════════════════════════════════════
    //  LOGIN OTP  (used by /auth/send-otp & /auth/verify-otp)
    // ══════════════════════════════════════════════════════════════════════

    public void sendOtp(String email) {
        String otp = generateOtp();
        redisTemplate.opsForValue()
                .set(OTP_LOGIN_PREFIX + email, otp, OTP_TTL_MINUTES, TimeUnit.MINUTES);

        publishOtpEvent(email, otp, "OTP_LOGIN",
                "OmniCharge – Your Login OTP",
                "Your login OTP is: " + otp + ". Valid for " + OTP_TTL_MINUTES + " minutes.");

        log.info("Login OTP event published to notification-service for {}", email);
    }

    public boolean verifyOtp(String email, String otp) {
        return verifyAndDelete(OTP_LOGIN_PREFIX + email, otp);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  FORGOT-PASSWORD OTP  (used by /auth/forgot-password & /auth/reset-password)
    // ══════════════════════════════════════════════════════════════════════

    public void sendForgotPasswordOtp(String email) {
        String otp = generateOtp();
        redisTemplate.opsForValue()
                .set(OTP_FORGOT_PREFIX + email, otp, OTP_TTL_MINUTES, TimeUnit.MINUTES);

        publishOtpEvent(email, otp, "OTP_FORGOT_PASSWORD",
                "OmniCharge – Password Reset OTP",
                "Your password-reset OTP is: " + otp +
                        ". Valid for " + OTP_TTL_MINUTES + " minutes. Do NOT share this with anyone.");

        log.info("Forgot-password OTP event published to notification-service for {}", email);
    }

    public boolean verifyForgotPasswordOtp(String email, String otp) {
        return verifyAndDelete(OTP_FORGOT_PREFIX + email, otp);
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private String generateOtp() {
        return String.valueOf(100000 + new SecureRandom().nextInt(900000));
    }

    private boolean verifyAndDelete(String redisKey, String otp) {
        String stored = redisTemplate.opsForValue().get(redisKey);
        if (stored != null && stored.equals(otp)) {
            redisTemplate.delete(redisKey);
            return true;
        }
        return false;
    }

    private void publishOtpEvent(String email, String otp,
                                 String type, String subject, String message) {
        NotificationEvent event = NotificationEvent.builder()
                .email(email)
                .message(message)
                .subject(subject)
                .type(type)
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.OTP_ROUTING_KEY,
                event
        );
    }
}