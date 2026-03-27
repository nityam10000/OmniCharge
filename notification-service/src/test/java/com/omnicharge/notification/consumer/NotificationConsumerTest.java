package com.omnicharge.notification.consumer;

import com.omnicharge.notification.dto.NotificationEvent;
import com.omnicharge.notification.service.EmailService;
import com.omnicharge.notification.service.WhatsAppService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private WhatsAppService whatsAppService;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    private NotificationEvent event;

    @BeforeEach
    void setUp() {
        event = new NotificationEvent();
        event.setMessage("Your recharge of Rs.299 was successful!");
        event.setEmail("rahul@example.com");
        event.setPhoneNumber("9876543210");
        event.setType("PAYMENT_SUCCESS");

        // optional fields used in buildMessage
        event.setRechargeId("RX123");
        event.setAmount(299.0);
        event.setMobile("9876543210");
        event.setOperator("Jio");
        event.setDate("2026-03-26");
    }

    // ✅ Happy path
    @Test
    @DisplayName("consume: should send email and WhatsApp when both present")
    void consume_ShouldSendEmailAndWhatsApp() {

        doNothing().when(emailService).sendEmail(anyString(), any(NotificationEvent.class));
        doNothing().when(whatsAppService).sendWhatsAppMessage(anyString(), anyString());

        notificationConsumer.consume(event);

        verify(emailService).sendEmail(eq("rahul@example.com"), eq(event));

        verify(whatsAppService).sendWhatsAppMessage(
                eq("9876543210"),
                anyString() // because buildMessage() generates it
        );
    }

    // ✅ Phone null → only email
    @Test
    void consume_ShouldSendOnlyEmail_WhenPhoneNull() {
        event.setPhoneNumber(null);

        notificationConsumer.consume(event);

        verify(emailService).sendEmail(eq("rahul@example.com"), eq(event));
        verify(whatsAppService, never()).sendWhatsAppMessage(anyString(), anyString());
    }

    // ✅ Phone blank → only email
    @Test
    void consume_ShouldSendOnlyEmail_WhenPhoneBlank() {
        event.setPhoneNumber("   ");

        notificationConsumer.consume(event);

        verify(emailService).sendEmail(eq("rahul@example.com"), eq(event));
        verify(whatsAppService, never()).sendWhatsAppMessage(anyString(), anyString());
    }

    // ✅ Email fails → WhatsApp still works
    @Test
    void consume_ShouldStillSendWhatsApp_WhenEmailFails() {

        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendEmail(anyString(), any(NotificationEvent.class));

        notificationConsumer.consume(event);

        verify(emailService).sendEmail(anyString(), any(NotificationEvent.class));
        verify(whatsAppService).sendWhatsAppMessage(anyString(), anyString());
    }

    // ✅ WhatsApp fails → no crash
    @Test
    void consume_ShouldNotThrow_WhenWhatsAppFails() {

        doThrow(new RuntimeException("Twilio error"))
                .when(whatsAppService).sendWhatsAppMessage(anyString(), anyString());

        notificationConsumer.consume(event);

        verify(emailService).sendEmail(anyString(), any(NotificationEvent.class));
        verify(whatsAppService).sendWhatsAppMessage(anyString(), anyString());
    }

    // ✅ Both fail → no crash
    @Test
    void consume_ShouldNotThrow_WhenBothFail() {

        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendEmail(anyString(), any(NotificationEvent.class));

        doThrow(new RuntimeException("Twilio error"))
                .when(whatsAppService).sendWhatsAppMessage(anyString(), anyString());

        notificationConsumer.consume(event);

        verify(emailService).sendEmail(anyString(), any(NotificationEvent.class));
        verify(whatsAppService).sendWhatsAppMessage(anyString(), anyString());
    }
}