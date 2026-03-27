package com.omnicharge.rechargeprocessing.consumer;

import com.omnicharge.rechargeprocessing.configuration.RabbitMQConfig;
import com.omnicharge.rechargeprocessing.dto.PaymentSagaEvent;
import com.omnicharge.rechargeprocessing.entity.Recharge;
import com.omnicharge.rechargeprocessing.enums.RechargeStatus;
import com.omnicharge.rechargeprocessing.exception.RechargeNotFoundException;
import com.omnicharge.rechargeprocessing.repository.IRechargeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Consumes saga events from payment.saga.queue.
 *
 * Replaces the old HTTP endpoint that PaymentService called via Feign
 * (rechargeClient.updateRechargeStatus). There is no HTTP call involved
 * anymore — communication is purely event-driven.
 *
 * On success: updates Recharge status → publishes "recharge.updated" reply.
 * On failure: throws exception → RabbitMQ retries → eventually DLQ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSagaConsumer {

    private final IRechargeRepository rechargeRepository;
    private final RabbitTemplate rabbitTemplate;

    /**
     * Listens on payment.saga.queue.
     * Receives both "payment.completed" and "payment.failed" events
     * (both are routed to this queue by PaymentService's RabbitMQConfig).
     *
     * Spring AMQP retry config (in application.properties) will re-deliver
     * the message up to 3 times on exception before routing to the DLQ.
     */
    @RabbitListener(queues = RabbitMQConfig.SAGA_PAYMENT_QUEUE)
    public void onPaymentSagaEvent(PaymentSagaEvent event) {
        log.info("Saga event received — sagaId={}, eventType={}, rechargeId={}",
                event.getSagaId(), event.getEventType(), event.getRechargeId());

        // Determine target status from event type
        RechargeStatus targetStatus = resolveTargetStatus(event.getEventType());

        // Find and update the recharge
        // If not found → throw → message will be retried, then go to DLQ
        Recharge recharge = rechargeRepository.findById(event.getRechargeId())
                .orElseThrow(() -> {
                    log.error("Recharge not found — sagaId={}, rechargeId={}",
                            event.getSagaId(), event.getRechargeId());
                    return new RechargeNotFoundException(
                            "Recharge not found for id: " + event.getRechargeId());
                });

        // Idempotency: if already updated (e.g. duplicate delivery), skip
        if (recharge.getStatus() == targetStatus) {
            log.info("Recharge {} already in status={} — skipping duplicate event",
                    event.getRechargeId(), targetStatus);
            publishReply(event);
            return;
        }

        recharge.setStatus(targetStatus);
        rechargeRepository.save(recharge);

        log.info("Recharge {} updated to {} — sagaId={}",
                event.getRechargeId(), targetStatus, event.getSagaId());

        // Publish reply back to PaymentService
        publishReply(event);
    }

    /**
     * Publishes "recharge.updated" reply onto the saga exchange.
     * PaymentService's SagaReplyConsumer picks this up to log saga completion.
     */
    private void publishReply(PaymentSagaEvent event) {
        try {
            PaymentSagaEvent reply = PaymentSagaEvent.builder()
                    .sagaId(event.getSagaId())
                    .transactionId(event.getTransactionId())
                    .rechargeId(event.getRechargeId())
                    .userId(event.getUserId())
                    .userEmail(event.getUserEmail())
                    .eventType(RabbitMQConfig.SAGA_REPLY_ROUTING) // "recharge.updated"
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.SAGA_EXCHANGE,        // "payment_saga_exchange"
                    RabbitMQConfig.SAGA_REPLY_ROUTING,   // "recharge.updated"
                    reply);

            log.info("Saga reply published — sagaId={}, routingKey=recharge.updated",
                    event.getSagaId());

        } catch (Exception e) {
            // Reply failure is non-critical — the main work (recharge update) is done.
            // Log and continue; PaymentService will not receive confirmation but
            // data is consistent.
            log.error("Failed to publish saga reply for sagaId={}: {}",
                    event.getSagaId(), e.getMessage());
        }
    }

    /**
     * Maps event type string to RechargeStatus enum.
     * Throws if an unknown event type arrives — will route to DLQ after retries.
     */
    private RechargeStatus resolveTargetStatus(String eventType) {
        return switch (eventType) {
            case "payment.completed" -> RechargeStatus.SUCCESS;
            case "payment.failed"    -> RechargeStatus.FAILED;
            default -> {
                log.error("Unknown saga event type: {}", eventType);
                throw new IllegalArgumentException("Unknown saga event type: " + eventType);
            }
        };
    }
}