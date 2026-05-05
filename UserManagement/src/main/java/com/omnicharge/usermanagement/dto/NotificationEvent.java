package com.omnicharge.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published to the shared notification_exchange.
 * Consumed by notification-service to send emails / WhatsApp messages.
 *
 * NOTE: Field names must match com.omnicharge.notification.dto.NotificationEvent
 *       exactly — Jackson deserialises by field name, not class name.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    private String message;
    private String email;
    private String phoneNumber;
    private String type;       // e.g. "WELCOME", "PAYMENT_SUCCESS"
    private String subject;    // optional — consumer uses this as email subject if set
}