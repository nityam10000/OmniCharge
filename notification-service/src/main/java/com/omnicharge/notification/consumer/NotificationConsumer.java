package com.omnicharge.notification.consumer;

import com.omnicharge.notification.dto.NotificationEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    @RabbitListener(queues = "notification_queue")
    public void consume(NotificationEvent event) {
        System.out.println("📩 Notification Received:");
        System.out.println("Message: " + event.getMessage());
        System.out.println("Email: " + event.getEmail());
    }
}
