package com.omnicharge.notification.service;

import com.omnicharge.notification.dto.NotificationEvent;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceBranchTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "fromEmail", "no-reply@omnicharge.com");
        mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendEmail_ShouldRenderOtpTemplate() throws Exception {
        NotificationEvent event = new NotificationEvent();
        event.setEmail("rahul@example.com");
        event.setType("OTP_LOGIN");
        event.setMessage("123456");

        emailService.sendEmail("rahul@example.com", event);

        assertEquals("OmniCharge OTP Verification", mimeMessage.getSubject());
        assertEquals("rahul@example.com", mimeMessage.getAllRecipients()[0].toString());
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_ShouldRenderRegistrationTemplate() throws Exception {
        NotificationEvent event = new NotificationEvent();
        event.setEmail("rahul@example.com");
        event.setType("WELCOME");
        event.setMessage("Welcome Rahul");

        emailService.sendEmail("rahul@example.com", event);

        assertEquals("Welcome to OmniCharge!", mimeMessage.getSubject());
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_ShouldRenderRefundTemplate() throws Exception {
        NotificationEvent event = new NotificationEvent();
        event.setEmail("rahul@example.com");
        event.setType("PAYMENT_REFUND");
        event.setMessage("Refund started");

        emailService.sendEmail("rahul@example.com", event);

        assertEquals("Refund Initiated - OmniCharge", mimeMessage.getSubject());
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_ShouldRenderFailedPaymentTemplate() throws Exception {
        NotificationEvent event = new NotificationEvent();
        event.setEmail("rahul@example.com");
        event.setType("PAYMENT_FAILED");
        event.setMessage("Payment failed");

        emailService.sendEmail("rahul@example.com", event);

        assertEquals("Payment Failed - OmniCharge", mimeMessage.getSubject());
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_ShouldRenderRechargeTemplate() throws Exception {
        NotificationEvent event = new NotificationEvent();
        event.setEmail("rahul@example.com");
        event.setRechargeId("RC123");
        event.setMobile("9876543210");
        event.setOperator("Jio");
        event.setAmount(299.0);
        event.setDate("2026-03-27");

        emailService.sendEmail("rahul@example.com", event);

        assertEquals("Recharge Successful - OmniCharge", mimeMessage.getSubject());
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_ShouldRenderGenericFallbackTemplate() throws Exception {
        NotificationEvent event = new NotificationEvent();
        event.setEmail("rahul@example.com");

        emailService.sendEmail("rahul@example.com", event, null);

        assertEquals("Notification from OmniCharge", mimeMessage.getSubject());
        verify(mailSender).send(mimeMessage);
    }
}
