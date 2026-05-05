package com.omnicharge.notification.service;

import com.omnicharge.notification.dto.NotificationEvent;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.mail.Session;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private MimeMessage mimeMessage;

    private NotificationEvent event;

    @BeforeEach
    void setup() {
        event = new NotificationEvent();
        event.setType("PAYMENT_SUCCESS");
        event.setMessage("Payment completed successfully");
        event.setSubject("Test Subject");

        mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        ReflectionTestUtils.setField(emailService, "fromEmail", "no-reply@omnicharge.com");
    }

    // ✅ HAPPY PATH
    @Test
    @DisplayName("sendEmail: should create and send MimeMessage")
    void sendEmail_ShouldSendMimeMessage() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail("test@example.com", event);

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    // ✅ VERIFY METHOD CALL
    @Test
    @DisplayName("sendEmail: should call mailSender.send() once")
    void sendEmail_ShouldCallSendOnce() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail("rahul@example.com", event);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    // ✅ FAILURE CASE (NO EXCEPTION EXPECTED)
    @Test
    @DisplayName("sendEmail: should handle exception internally")
    void sendEmail_ShouldHandleException() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("SMTP failure"))
                .when(mailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() ->
                emailService.sendEmail("test@example.com", event)
        );

        verify(mailSender).send(any(MimeMessage.class));
    }
}
