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

<<<<<<< HEAD
    @Mock private EmailService emailService;
    @Mock private WhatsAppService whatsAppService;
=======
    @Mock
    private EmailService emailService;

    @Mock
    private WhatsAppService whatsAppService;
>>>>>>> origin/bhavik

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    private NotificationEvent event;

    @BeforeEach
    void setUp() {
<<<<<<< HEAD
        event = new NotificationEvent(
                "Your recharge of Rs.299 was successful!",
                "rahul@example.com",
                "9876543210",
                "PAYMENT_SUCCESS"
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // consume() — happy path: both email and WhatsApp sent
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("consume: should send email and WhatsApp when both email and phone are present")
    void consume_ShouldSendEmailAndWhatsApp_WhenBothPresent() {
        doNothing().when(emailService).sendEmail(anyString(), anyString());
        doNothing().when(whatsAppService).sendWhatsAppMessage(anyString(), anyString());

        notificationConsumer.consume(event);

        verify(emailService).sendEmail("rahul@example.com",
                "Your recharge of Rs.299 was successful!");
        verify(whatsAppService).sendWhatsAppMessage("9876543210",
                "Your recharge of Rs.299 was successful!");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // consume() — phone number is null: only email sent, WhatsApp skipped
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("consume: should send only email when phone number is null")
    void consume_ShouldSendOnlyEmail_WhenPhoneIsNull() {
        event.setPhoneNumber(null);
        doNothing().when(emailService).sendEmail(anyString(), anyString());

        notificationConsumer.consume(event);

        verify(emailService).sendEmail("rahul@example.com",
                "Your recharge of Rs.299 was successful!");
        verify(whatsAppService, never()).sendWhatsAppMessage(anyString(), anyString());
    }

    @Test
    @DisplayName("consume: should send only email when phone number is blank")
    void consume_ShouldSendOnlyEmail_WhenPhoneIsBlank() {
        event.setPhoneNumber("   ");
        doNothing().when(emailService).sendEmail(anyString(), anyString());

        notificationConsumer.consume(event);

        verify(emailService).sendEmail(anyString(), anyString());
        verify(whatsAppService, never()).sendWhatsAppMessage(anyString(), anyString());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // consume() — email fails: should NOT crash the consumer,
    // WhatsApp should still be attempted
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("consume: should still attempt WhatsApp even if email throws an exception")
    void consume_ShouldStillSendWhatsApp_WhenEmailFails() {
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendEmail(anyString(), anyString());
        doNothing().when(whatsAppService).sendWhatsAppMessage(anyString(), anyString());

        // consumer catches the exception internally — must NOT propagate
        notificationConsumer.consume(event);

        verify(emailService).sendEmail(anyString(), anyString());
        verify(whatsAppService).sendWhatsAppMessage(anyString(), anyString());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // consume() — WhatsApp fails: should NOT crash the consumer
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("consume: should not throw when WhatsApp fails — exception is caught internally")
    void consume_ShouldNotThrow_WhenWhatsAppFails() {
        doNothing().when(emailService).sendEmail(anyString(), anyString());
        doThrow(new RuntimeException("Twilio error"))
                .when(whatsAppService).sendWhatsAppMessage(anyString(), anyString());

        // consumer catches the exception — must NOT propagate
        notificationConsumer.consume(event);

        verify(emailService).sendEmail(anyString(), anyString());
        verify(whatsAppService).sendWhatsAppMessage(anyString(), anyString());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // consume() — both email and WhatsApp fail: consumer still must not crash
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("consume: should not throw when both email and WhatsApp fail")
    void consume_ShouldNotThrow_WhenBothFail() {
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendEmail(anyString(), anyString());
=======
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

>>>>>>> origin/bhavik
        doThrow(new RuntimeException("Twilio error"))
                .when(whatsAppService).sendWhatsAppMessage(anyString(), anyString());

        notificationConsumer.consume(event);

<<<<<<< HEAD
        verify(emailService).sendEmail(anyString(), anyString());
=======
        verify(emailService).sendEmail(anyString(), any(NotificationEvent.class));
>>>>>>> origin/bhavik
        verify(whatsAppService).sendWhatsAppMessage(anyString(), anyString());
    }
}