package com.omnicharge.paymentservice.consumer;

import com.omnicharge.paymentservice.configuration.RabbitMQConfig;
import com.omnicharge.paymentservice.dto.PaymentSagaEvent;
import com.omnicharge.paymentservice.entity.Transaction;
import com.omnicharge.paymentservice.enums.TransactionStatus;
import com.omnicharge.paymentservice.repository.ITransactionRepository;
import com.omnicharge.paymentservice.service.RazorpayRefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Listens on TWO queues:
 *
 * 1. recharge.saga.reply.queue
 *    Happy path — RechargeProcessing successfully updated its status
 *    and published a "recharge.updated" event back. Saga is complete.
 *
 * 2. saga.dead.letter.queue
 *    Compensation path — a saga event failed all retries and landed
 *    in the DLQ. We mark the transaction FAILED, issue a real Razorpay
 *    refund, and send a refund email notification to the user.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SagaReplyConsumer {

    private final ITransactionRepository transactionRepository;
    private final RazorpayRefundService razorpayRefundService; // ← NEW


    //  Happy path reply — unchanged


    @RabbitListener(queues = RabbitMQConfig.SAGA_REPLY_QUEUE)
    public void onRechargeUpdated(PaymentSagaEvent event) {
        log.info("SAGA COMPLETE — sagaId={}, rechargeId={}, eventType={}",
                event.getSagaId(), event.getRechargeId(), event.getEventType());
    }

    //  Compensation — DLQ handler (UPDATED: real refund + notification)

    @RabbitListener(queues = RabbitMQConfig.SAGA_DLQ)
    public void onSagaDeadLetter(PaymentSagaEvent event) {
        log.error("💀 SAGA COMPENSATION TRIGGERED — sagaId={}, rechargeId={}, reason: landed in DLQ",
                event.getSagaId(), event.getRechargeId());

        try {
            UUID transactionId = event.getTransactionId();

            transactionRepository.findById(transactionId).ifPresentOrElse(txn -> {

                // Only compensate if the transaction was SUCCESS (not already FAILED)
                if (txn.getStatus() == TransactionStatus.SUCCESS) {

                    txn.setStatus(TransactionStatus.FAILED);
                    txn.setFailureReason(
                            "Saga compensation: recharge activation failed after all retries. Refund initiated.");
                    transactionRepository.save(txn);

                    log.warn("Transaction {} rolled back to FAILED during saga compensation",
                            transactionId);

                    // Issue real Razorpay refund + send refund email
                    if (txn.getRazorpayPaymentId() != null && !txn.getRazorpayPaymentId().isBlank()) {
                        razorpayRefundService.refundAndNotify(
                                txn,
                                "Saga compensation: recharge activation failed for rechargeId=" + txn.getRechargeId()
                        );
                    } else {
                        log.error("🚨 CRITICAL: Transaction {} has no razorpayPaymentId — " +
                                "cannot issue automatic refund. Manual refund required.", transactionId);
                    }

                } else {
                    log.info("Transaction {} already in status={} — no compensation needed",
                            transactionId, txn.getStatus());
                }

            }, () -> log.error("Compensation failed — transaction not found: {}", transactionId));

        } catch (Exception e) {
            // Do NOT re-throw — re-throwing causes an infinite DLQ loop
            log.error("CRITICAL: Saga compensation handler failed for sagaId={}. " +
                            "Manual intervention required. Error: {}",
                    event.getSagaId(), e.getMessage(), e);
        }
    }
}