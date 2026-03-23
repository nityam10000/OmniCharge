package com.omnicharge.notification.consumer;

import com.omnicharge.notification.dto.NotificationEvent;
import com.omnicharge.notification.service.EmailService;
import com.omnicharge.notification.service.WhatsAppService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final EmailService emailService;
    private final WhatsAppService whatsAppService;

    public NotificationConsumer(EmailService emailService,
                                WhatsAppService whatsAppService) {
        this.emailService = emailService;
        this.whatsAppService = whatsAppService;
    }

    @RabbitListener(queues = "notification_queue")
    public void consume(NotificationEvent event) {

        System.out.println("📩 Notification Received:");
        System.out.println("Message: " + event.getMessage());
        System.out.println("Email: " + event.getEmail());
        System.out.println("Phone: " + event.getPhoneNumber());

        // ✅ Email — wrapped in try/catch so failure doesn't requeue the message
        try {
            emailService.sendEmail(event.getEmail(), event.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Failed to send email to " + event.getEmail() + ": " + e.getMessage());
        }

        // ✅ WhatsApp — wrapped separately so Twilio failure never affects email
        // and never causes message requeue
        try {
            if (event.getPhoneNumber() != null && !event.getPhoneNumber().isBlank()) {
                whatsAppService.sendWhatsAppMessage(
                        event.getPhoneNumber(),
                        event.getMessage()
                );
            } else {
                System.out.println("⚠️ No phone number provided — skipping WhatsApp");
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to send WhatsApp: " + e.getMessage());
        }
    }
}