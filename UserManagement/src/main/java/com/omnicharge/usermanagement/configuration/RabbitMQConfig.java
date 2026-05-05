package com.omnicharge.usermanagement.configuration;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE    = "notification_exchange";
    public static final String ROUTING_KEY = "notification_routing";

    @Value("${spring.rabbitmq.host:localhost}")
    private String host;

    @Value("${spring.rabbitmq.port:5672}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(host, port);
        factory.setUsername(username);
        factory.setPassword(password);
        // Auto-recover connection if RabbitMQ drops
        factory.getRabbitConnectionFactory().setAutomaticRecoveryEnabled(true);
        factory.getRabbitConnectionFactory().setNetworkRecoveryInterval(5000); // retry every 5s
        factory.getRabbitConnectionFactory().setRequestedHeartbeat(30);
        return factory;
    }

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(EXCHANGE);
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

        // Retry up to 3 times with exponential backoff before giving up
        RetryTemplate retryTemplate = new RetryTemplate();

        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(1000);   // wait 1s
        backOff.setMultiplier(2.0);         // then 2s, then 4s
        backOff.setMaxInterval(10000);      // cap at 10s

        retryTemplate.setBackOffPolicy(backOff);
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(3));
        template.setRetryTemplate(retryTemplate);

        return template;
    }
}