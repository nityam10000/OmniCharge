package com.omnicharge.notification.service;

<<<<<<< HEAD
=======
import com.omnicharge.notification.dto.NotificationEvent;
import jakarta.mail.internet.MimeMessage;
>>>>>>> origin/bhavik
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
<<<<<<< HEAD
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
=======
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
>>>>>>> origin/bhavik
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

<<<<<<< HEAD
    // JavaMailSender is faked — no real SMTP server needed
    @Mock private JavaMailSender mailSender;
=======
    @Mock
    private JavaMailSender mailSender;
>>>>>>> origin/bhavik

    @InjectMocks
    private EmailService emailService;

<<<<<<< HEAD
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
=======
    @Mock
    private MimeMessage mimeMessage;

    private NotificationEvent event;

    @BeforeEach
    void setup() {
        event = new NotificationEvent();
        event.setType("PAYMENT_SUCCESS");
        event.setMessage("Payment completed successfully");
        event.setSubject("Test Subject");
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
>>>>>>> origin/bhavik
    }
}