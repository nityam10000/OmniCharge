package com.omnicharge.notification.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhatsAppServiceTest {

    @InjectMocks
    private WhatsAppService whatsAppService;

    @BeforeEach
    void setUp() throws Exception {
        // Inject the @Value field since there is no Spring context in unit tests
        setField(whatsAppService, "fromNumber", "+14155238886");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // sendWhatsAppMessage() — happy path
    //
    // Twilio's Message.creator() is a static factory method, so we use
    // MockedStatic to intercept it without making a real API call.
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("sendWhatsAppMessage: should call Twilio Message.creator and create the message")
    void sendWhatsAppMessage_ShouldCallTwilioCreator() {
        // Create fake Twilio objects
        MessageCreator mockCreator = mock(MessageCreator.class);
        Message mockMessage = mock(Message.class);

        when(mockCreator.create()).thenReturn(mockMessage);
        when(mockMessage.getSid()).thenReturn("SM_TEST_SID_12345");

        // Create args BEFORE opening MockedStatic — avoids UnfinishedStubbingException
        try (MockedStatic<Message> messageMock = mockStatic(Message.class)) {
            messageMock.when(() -> Message.creator(
                    any(PhoneNumber.class),
                    any(PhoneNumber.class),
                    anyString()
            )).thenReturn(mockCreator);

            whatsAppService.sendWhatsAppMessage("9876543210",
                    "Your recharge was successful!");

            // Verify creator was called with correct whatsapp: prefix on the to-number
            messageMock.verify(() -> Message.creator(
                    eq(new PhoneNumber("whatsapp:9876543210")),
                    eq(new PhoneNumber("+14155238886")),
                    eq("Your recharge was successful!")
            ));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // sendWhatsAppMessage() — Twilio API fails: exception must propagate
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("sendWhatsAppMessage: should throw exception when Twilio API call fails")
    void sendWhatsAppMessage_ShouldThrowException_WhenTwilioFails() {
        MessageCreator mockCreator = mock(MessageCreator.class);

        when(mockCreator.create()).thenThrow(new RuntimeException("Twilio API error"));

        try (MockedStatic<Message> messageMock = mockStatic(Message.class)) {
            messageMock.when(() -> Message.creator(
                    any(PhoneNumber.class),
                    any(PhoneNumber.class),
                    anyString()
            )).thenReturn(mockCreator);

            RuntimeException thrown = assertThrows(RuntimeException.class,
                    () -> whatsAppService.sendWhatsAppMessage("9876543210", "Test"));

            assertEquals("Twilio API error", thrown.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Helper — injects @Value fields via reflection (no Spring context needed)
    // ══════════════════════════════════════════════════════════════════════════

    private void setField(Object target, String fieldName, String value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}