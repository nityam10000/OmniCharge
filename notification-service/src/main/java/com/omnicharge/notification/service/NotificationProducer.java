package com.omnicharge.notification.service;

import com.omnicharge.notification.configuration.RabbitMQConfig;
import com.omnicharge.notification.dto.NotificationEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public NotificationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendNotification(NotificationEvent event) {

        System.out.println("📤 Sending Notification:");
        System.out.println("Message: " + event.getMessage());
        System.out.println("Email: " + event.getEmail());
        System.out.println("Phone: " + event.getPhoneNumber());
        System.out.println("Type: " + event.getType());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                event
        );
    }
}