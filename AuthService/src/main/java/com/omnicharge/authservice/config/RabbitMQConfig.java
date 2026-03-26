package com.omnicharge.authservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE       = "notification_queue";
    public static final String EXCHANGE    = "notification_exchange";
    public static final String ROUTING_KEY = "notification_routing";

    // ── OTP-specific routing key (same exchange, dedicated key) ──────────
    public static final String OTP_ROUTING_KEY = "otp_notification_routing";

    @Bean
    public Queue otpQueue() {
        return new Queue("otp_notification_queue", true);
    }

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding otpBinding(Queue otpQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(otpQueue).to(notificationExchange).with(OTP_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}