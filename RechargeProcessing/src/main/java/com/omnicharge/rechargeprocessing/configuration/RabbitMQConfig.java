package com.omnicharge.rechargeprocessing.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for RechargeProcessing.
 *
 * This service is a CONSUMER of payment saga events and a PRODUCER
 * of the reply event (recharge.updated).
 *
 * Queue/exchange names must exactly match RabbitMQConfig in PaymentService.
 */
@Configuration
public class RabbitMQConfig {

    // ── Names must match PaymentService RabbitMQConfig exactly ───
    public static final String SAGA_EXCHANGE          = "payment_saga_exchange";
    public static final String SAGA_PAYMENT_QUEUE     = "payment.saga.queue";
    public static final String SAGA_ROUTING_COMPLETED = "payment.completed";
    public static final String SAGA_ROUTING_FAILED    = "payment.failed";
    public static final String SAGA_REPLY_ROUTING     = "recharge.updated";
    public static final String SAGA_DLQ_EXCHANGE      = "saga.dead.letter.exchange";
    public static final String SAGA_DLQ               = "saga.dead.letter.queue";

    // ── Dead-letter exchange (must be declared here too) ─────────
    @Bean
    public DirectExchange sagaDeadLetterExchange() {
        return new DirectExchange(SAGA_DLQ_EXCHANGE);
    }

    @Bean
    public Queue sagaDeadLetterQueue() {
        return QueueBuilder.durable(SAGA_DLQ).build();
    }

    // ── Saga exchange (declared here for idempotency) ─────────────
    // RabbitMQ is idempotent on exchange/queue declare — safe to
    // declare in multiple services as long as arguments match.
    @Bean
    public DirectExchange sagaExchange() {
        return new DirectExchange(SAGA_EXCHANGE);
    }

    // ── payment.saga.queue (this service consumes from it) ────────
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

    // ── Shared infra ──────────────────────────────────────────────
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