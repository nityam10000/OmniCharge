package com.omnicharge.notification.controller;

import com.omnicharge.notification.dto.NotificationEvent;
import com.omnicharge.notification.service.NotificationProducer;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notify")
public class NotificationController {

    private final NotificationProducer producer;

    public NotificationController(NotificationProducer producer) {
        this.producer = producer;
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
}