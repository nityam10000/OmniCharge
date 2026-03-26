package com.omnicharge.notification.controller;

import com.omnicharge.notification.dto.NotificationEvent;
import com.omnicharge.notification.service.EmailService;
import com.omnicharge.notification.service.NotificationProducer;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notify")
public class NotificationController {

    private final NotificationProducer producer;
    private final EmailService emailService;

    public NotificationController(NotificationProducer producer, EmailService emailService) {
        this.producer = producer;
        this.emailService = emailService;
    }

    @PostMapping
    public String sendNotification(@RequestBody NotificationEvent event) {

        // Debug logs (VERY IMPORTANT for testing)
        System.out.println("📥 Notification Request Received:");
        System.out.println("Message: " + event.getMessage());
        System.out.println("Email: " + event.getEmail());
        System.out.println("Phone: " + event.getPhoneNumber());
        System.out.println("Type: " + event.getType());

        producer.sendNotification(event);

        return "Notification sent successfully!";
    }

    @PostMapping("/email/otp")
    public String sendOtpEmail(@RequestBody NotificationEvent event) {
        return sendEmailByType(event, "OTP_LOGIN", "OTP email sent successfully!");
    }

    @PostMapping("/email/register")
    public String sendRegisterEmail(@RequestBody NotificationEvent event) {
        return sendEmailByType(event, "WELCOME", "Registration email sent successfully!");
    }

    @PostMapping("/email/refund")
    public String sendRefundEmail(@RequestBody NotificationEvent event) {
        return sendEmailByType(event, "PAYMENT_REFUND", "Refund email sent successfully!");
    }

    @PostMapping("/email/transaction/success")
    public String sendTransactionSuccessEmail(@RequestBody NotificationEvent event) {
        return sendEmailByType(event, "PAYMENT_SUCCESS", "Transaction success email sent successfully!");
    }

    @PostMapping("/email/transaction/failed")
    public String sendTransactionFailedEmail(@RequestBody NotificationEvent event) {
        return sendEmailByType(event, "PAYMENT_FAILED", "Transaction failure email sent successfully!");
    }

    private String sendEmailByType(NotificationEvent event, String defaultType, String successMessage) {
        if (event.getType() == null || event.getType().isBlank()) {
            event.setType(defaultType);
        }

        emailService.sendEmail(event.getEmail(), event);
        return successMessage;
    }
}
