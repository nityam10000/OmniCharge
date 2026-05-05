package com.omnicharge.notification.controller;

import com.omnicharge.notification.dto.NotificationEvent;
import com.omnicharge.notification.service.EmailService;
import com.omnicharge.notification.service.NotificationProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationProducer producer;

    @Mock
    private EmailService emailService;

    private NotificationController controller;
    private NotificationEvent event;

    @BeforeEach
    void setUp() {
        controller = new NotificationController(producer, emailService);
        event = new NotificationEvent();
        event.setEmail("rahul@example.com");
        event.setPhoneNumber("9876543210");
        event.setMessage("Hello");
    }

    @Test
    void sendNotification_ShouldDelegateToProducer() {
        String response = controller.sendNotification(event);

        assertEquals("Notification sent successfully!", response);
        verify(producer).sendNotification(event);
    }

    @Test
    void sendOtpEmail_ShouldApplyDefaultType() {
        String response = controller.sendOtpEmail(event);

        assertEquals("OTP email sent successfully!", response);
        assertEquals("OTP_LOGIN", event.getType());
        verify(emailService).sendEmail("rahul@example.com", event);
    }

    @Test
    void sendRegisterEmail_ShouldPreserveExistingType() {
        event.setType("CUSTOM");

        String response = controller.sendRegisterEmail(event);

        assertEquals("Registration email sent successfully!", response);
        assertEquals("CUSTOM", event.getType());
        verify(emailService).sendEmail("rahul@example.com", event);
    }

    @Test
    void sendRefundEmail_ShouldApplyRefundType() {
        String response = controller.sendRefundEmail(event);

        assertEquals("Refund email sent successfully!", response);
        assertEquals("PAYMENT_REFUND", event.getType());
    }

    @Test
    void sendTransactionSuccessEmail_ShouldApplySuccessType() {
        String response = controller.sendTransactionSuccessEmail(event);

        assertEquals("Transaction success email sent successfully!", response);
        assertEquals("PAYMENT_SUCCESS", event.getType());
    }

    @Test
    void sendTransactionFailedEmail_ShouldApplyFailedType() {
        String response = controller.sendTransactionFailedEmail(event);

        assertEquals("Transaction failure email sent successfully!", response);
        assertEquals("PAYMENT_FAILED", event.getType());
    }
}
