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
        producer.sendNotification(event);
        return "Notification sent!";
    }
}