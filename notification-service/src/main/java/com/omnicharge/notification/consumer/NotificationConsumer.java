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

        // ✅ Email send
        emailService.sendEmail(event.getEmail(), event.getMessage());

        // ✅ WhatsApp send
        whatsAppService.sendWhatsAppMessage(
                event.getPhoneNumber(),
                event.getMessage()
        );
    }
}