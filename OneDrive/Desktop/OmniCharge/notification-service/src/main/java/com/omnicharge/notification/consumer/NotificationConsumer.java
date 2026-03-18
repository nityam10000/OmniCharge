package com.omnicharge.notification.consumer;

import com.omnicharge.notification.dto.NotificationEvent;
import com.omnicharge.notification.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final EmailService emailService;

    public NotificationConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "notification_queue")
    public void consume(NotificationEvent event) {

        System.out.println("📩 Notification Received:");
        System.out.println("Message: " + event.getMessage());
        System.out.println("Email: " + event.getEmail());


        emailService.sendEmail(event.getEmail(), event.getMessage());
    }
}