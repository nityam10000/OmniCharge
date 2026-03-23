package com.omnicharge.notification.service;

import com.omnicharge.notification.configuration.RabbitMQConfig;
import com.omnicharge.notification.dto.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public NotificationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendNotification(NotificationEvent event) {

        log.info("Publishing notification event to RabbitMQ");
        log.debug("Notification Details - Type: {}, Email: {}, Phone: {}, Message: {}", 
                  event.getType(), event.getEmail(), event.getPhoneNumber(), event.getMessage());

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY,
                    event
            );
            log.info("Notification event published successfully - Type: {}, Recipient: {}", 
                     event.getType(), event.getEmail());
        } catch (Exception e) {
            log.error("Failed to publish notification event - Type: {}, Recipient: {}", 
                      event.getType(), event.getEmail(), e);
            throw e;
        }
    }
}