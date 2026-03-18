package com.omnicharge.notification.service;

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
        rabbitTemplate.convertAndSend(
                "notification_exchange",
                "notification_routing",
                event
        );
    }
}