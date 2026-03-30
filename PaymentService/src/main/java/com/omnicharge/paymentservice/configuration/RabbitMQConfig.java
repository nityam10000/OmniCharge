package com.omnicharge.paymentservice.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {


    // ─────────────────────────────────────────────────────────────
    //  EXISTING — notification (keep as-is, nothing changed here)
    // ─────────────────────────────────────────────────────────────

    public static final String QUEUE       = "notification_queue";
    public static final String EXCHANGE    = "notification_exchange";
    public static final String ROUTING_KEY = "notification_routing";

    @Bean
    public Queue notificationQueue() {

        return new Queue(QUEUE, true); // durable=true
    }

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean

    public Binding notificationBinding(Queue notificationQueue,
                                       DirectExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(notificationExchange).with(ROUTING_KEY);
    }

    // ─────────────────────────────────────────────────────────────
    //  NEW — Saga: PaymentService publishes here
    // ─────────────────────────────────────────────────────────────
    public static final String SAGA_EXCHANGE              = "payment_saga_exchange";

    // PaymentService → RechargeProcessing
    public static final String SAGA_PAYMENT_QUEUE         = "payment.saga.queue";
    public static final String SAGA_ROUTING_COMPLETED     = "payment.completed";
    public static final String SAGA_ROUTING_FAILED        = "payment.failed";

    // RechargeProcessing → PaymentService (reply)
    public static final String SAGA_REPLY_QUEUE           = "recharge.saga.reply.queue";
    public static final String SAGA_REPLY_ROUTING         = "recharge.updated";

    // Dead-letter queue — catches messages that fail all retries
    public static final String SAGA_DLQ                   = "saga.dead.letter.queue";
    public static final String SAGA_DLQ_EXCHANGE          = "saga.dead.letter.exchange";

    // ── Dead-letter exchange & queue ──────────────────────────────
    @Bean
    public DirectExchange sagaDeadLetterExchange() {
        return new DirectExchange(SAGA_DLQ_EXCHANGE);
    }

    @Bean
    public Queue sagaDeadLetterQueue() {
        return QueueBuilder.durable(SAGA_DLQ).build();
    }

    @Bean

    public Binding sagaDlqBinding(Queue sagaDeadLetterQueue,
                                  DirectExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(sagaDeadLetterQueue)
                .to(sagaDeadLetterExchange).with(SAGA_DLQ);
    }

    // ── Main saga exchange ────────────────────────────────────────
    @Bean
    public DirectExchange sagaExchange() {
        return new DirectExchange(SAGA_EXCHANGE);
    }

    // ── payment.saga.queue (consumed by RechargeProcessing) ───────
    // Points failed messages to the dead-letter exchange
    @Bean
    public Queue paymentSagaQueue() {
        return QueueBuilder.durable(SAGA_PAYMENT_QUEUE)
                .withArgument("x-dead-letter-exchange", SAGA_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", SAGA_DLQ)
                .build();
    }

    @Bean

    public Binding paymentSagaCompletedBinding(Queue paymentSagaQueue,
                                               DirectExchange sagaExchange) {
        return BindingBuilder.bind(paymentSagaQueue)
                .to(sagaExchange).with(SAGA_ROUTING_COMPLETED);
    }

    @Bean

    public Binding paymentSagaFailedBinding(Queue paymentSagaQueue,
                                            DirectExchange sagaExchange) {
        return BindingBuilder.bind(paymentSagaQueue)
                .to(sagaExchange).with(SAGA_ROUTING_FAILED);
    }

    // ── recharge.saga.reply.queue (consumed by PaymentService) ────
    @Bean
    public Queue sagaReplyQueue() {
        return QueueBuilder.durable(SAGA_REPLY_QUEUE).build();
    }

    @Bean

    public Binding sagaReplyBinding(Queue sagaReplyQueue,
                                    DirectExchange sagaExchange) {
        return BindingBuilder.bind(sagaReplyQueue)
                .to(sagaExchange).with(SAGA_REPLY_ROUTING);
    }

    // ── Shared infra beans ────────────────────────────────────────

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