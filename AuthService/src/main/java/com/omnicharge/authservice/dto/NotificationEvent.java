package com.omnicharge.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    private String email;
    private String phoneNumber;
    private String message;
    private String type;        // "OTP_LOGIN" | "OTP_FORGOT_PASSWORD"
    private String subject;     // email subject line
}