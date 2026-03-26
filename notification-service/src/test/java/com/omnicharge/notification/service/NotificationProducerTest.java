package com.omnicharge.notification.service;

import com.omnicharge.notification.configuration.RabbitMQConfig;
import com.omnicharge.notification.dto.NotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationProducerTest {

    // RabbitTemplate is faked — no real RabbitMQ broker needed
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NotificationProducer notificationProducer;

    private NotificationEvent event;

    @BeforeEach
    void setUp() {
        event = new NotificationEvent(
                "Your recharge of Rs.299 was successful!",
                "rahul@example.com",
                "9876543210",
                "PAYMENT_SUCCESS"
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // sendNotification() — happy path
    // We use ArgumentCaptor to confirm the correct exchange, routing key,
    // and event object were passed to convertAndSend()
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("sendNotification: should publish event to correct exchange and routing key")
    void sendNotification_ShouldPublishToCorrectExchangeAndRoutingKey() {
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        notificationProducer.sendNotification(event);

        ArgumentCaptor<String> exchangeCaptor  = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingCaptor   = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCaptor   = ArgumentCaptor.forClass(Object.class);

        verify(rabbitTemplate).convertAndSend(
                exchangeCaptor.capture(),
                routingCaptor.capture(),
                payloadCaptor.capture()
        );

        assertEquals(RabbitMQConfig.EXCHANGE,     exchangeCaptor.getValue());
        assertEquals(RabbitMQConfig.ROUTING_KEY,  routingCaptor.getValue());

        NotificationEvent published = (NotificationEvent) payloadCaptor.getValue();
        assertEquals("rahul@example.com",                          published.getEmail());
        assertEquals("9876543210",                                 published.getPhoneNumber());
        assertEquals("Your recharge of Rs.299 was successful!",    published.getMessage());
        assertEquals("PAYMENT_SUCCESS",                            published.getType());
    }

    @Test
    @DisplayName("sendNotification: should call convertAndSend exactly once")
    void sendNotification_ShouldCallConvertAndSendExactlyOnce() {
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        notificationProducer.sendNotification(event);

        verify(rabbitTemplate, times(1))
                .convertAndSend(anyString(), anyString(), any(Object.class));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // sendNotification() — RabbitMQ fails: exception must propagate
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("sendNotification: should throw exception when RabbitMQ publish fails")
    void sendNotification_ShouldThrowException_WhenRabbitFails() {
        doThrow(new RuntimeException("RabbitMQ connection refused"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> notificationProducer.sendNotification(event));

        assertEquals("RabbitMQ connection refused", thrown.getMessage());
    }
}