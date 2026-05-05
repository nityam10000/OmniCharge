package com.omnicharge.notification.consumer;

import com.omnicharge.notification.configuration.RabbitMQConfig;
import com.omnicharge.notification.dto.NotificationEvent;
import com.omnicharge.notification.service.EmailService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;


    @RabbitListener(queues = {
            RabbitMQConfig.QUEUE,
            RabbitMQConfig.OTP_QUEUE
    })
    public void consume(NotificationEvent event) {

        System.out.println("Notification Received:");
        System.out.println("Email: " + event.getEmail());
        System.out.println("Phone: " + event.getPhoneNumber());
        System.out.println("Type: " + event.getType());

        // ================= EMAIL =================
        try {
            if (event.getEmail() != null && !event.getEmail().isBlank()) {
                emailService.sendEmail(event.getEmail(), event);
            } else {
                System.out.println("No email provided — skipping email");
            }
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    private String buildMessage(NotificationEvent event) {
        if (event.getMessage() != null && !event.getMessage().isBlank()) {
            return event.getMessage();
        }
        if (event.getRechargeId() != null && !event.getRechargeId().isBlank()) {
            return "Recharge of Rs." + event.getAmount() +
                    " for " + event.getMobile() +
                    " (" + event.getOperator() + ") successful.\n" +
                    "Recharge ID: " + event.getRechargeId() +
                    "\nDate: " + event.getDate();
        }
        return "You have received a new notification from OmniCharge.";
    }
}