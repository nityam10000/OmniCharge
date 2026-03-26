package com.omnicharge.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    // JavaMailSender is faked — no real SMTP server needed
    @Mock private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    // ══════════════════════════════════════════════════════════════════════════
    // sendEmail() — happy path
    // ArgumentCaptor lets us inspect the exact SimpleMailMessage
    // that was passed to mailSender.send()
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("sendEmail: should build correct mail message and call mailSender.send()")
    void sendEmail_ShouldSendCorrectEmail() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendEmail("rahul@example.com", "Your recharge was successful!");

        // Capture the actual SimpleMailMessage passed to send()
        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertArrayEquals(new String[]{"rahul@example.com"}, sent.getTo());
        assertEquals("Recharge Status", sent.getSubject());
        assertEquals("Your recharge was successful!", sent.getText());
    }

    @Test
    @DisplayName("sendEmail: should call mailSender.send() exactly once")
    void sendEmail_ShouldCallSendExactlyOnce() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendEmail("test@example.com", "Test message");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // sendEmail() — failure path
    // The service re-throws the exception after logging — we verify that
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("sendEmail: should throw exception when mailSender fails")
    void sendEmail_ShouldThrowException_WhenMailSenderFails() {
        doThrow(new RuntimeException("SMTP connection refused"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> emailService.sendEmail("rahul@example.com", "Test"));

        assertEquals("SMTP connection refused", thrown.getMessage());
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}