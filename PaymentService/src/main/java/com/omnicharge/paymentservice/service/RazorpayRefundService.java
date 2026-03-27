package com.omnicharge.paymentservice.service;

import com.omnicharge.paymentservice.configuration.RabbitMQConfig;
import com.omnicharge.paymentservice.dto.NotificationEvent;
import com.omnicharge.paymentservice.entity.Transaction;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Centralized Razorpay refund logic.
 * Injected by both TransactionService and SagaReplyConsumer
 * to avoid circular dependencies and code duplication.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RazorpayRefundService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    // ─────────────────────────────────────────────────────────────
    //  Core refund + notification
    // ─────────────────────────────────────────────────────────────

    /**
     * Issues a full refund via Razorpay and publishes a PAYMENT_REFUND
     * notification event so the user receives an email.
     *
     * @param txn    The failed/compensated Transaction (must have razorpayPaymentId & amount set)
     * @param reason Human-readable reason stored in Razorpay notes (for ops visibility)
     */
    public void refundAndNotify(Transaction txn, String reason) {
        String refundId = initiateRazorpayRefund(txn.getRazorpayPaymentId(), txn.getAmount(), reason);
        sendRefundNotification(txn, refundId);
    }

    // ─────────────────────────────────────────────────────────────
    //  Razorpay API call
    // ─────────────────────────────────────────────────────────────

    /**
     * Calls Razorpay's POST /payments/{paymentId}/refund endpoint.
     *
     * @return refundId if successful, null if the call fails
     *         (failure is logged as CRITICAL — never rethrown so caller flow is not broken)
     */
    public String initiateRazorpayRefund(String razorpayPaymentId, Double amount, String reason) {
        if (razorpayPaymentId == null || razorpayPaymentId.isBlank()) {
            log.warn("Skipping Razorpay refund — razorpayPaymentId is null/blank. reason={}", reason);
            return null;
        }
        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", (int) (amount * 100)); // paise
            refundRequest.put("speed", "normal");              // "optimum" for instant refund (extra cost)
            refundRequest.put("notes", new JSONObject().put("reason", reason));

            com.razorpay.Refund refund = client.payments.refund(razorpayPaymentId, refundRequest);
            String refundId = refund.get("id");

            log.info("✅ Razorpay refund initiated — refundId={}, paymentId={}, amount={}",
                    refundId, razorpayPaymentId, amount);
            return refundId;

        } catch (Exception e) {
            // CRITICAL: If refund fails, money is stuck. Alert ops.
            // In production hook this into PagerDuty / Slack / alerting system.
            log.error("🚨 CRITICAL: Razorpay refund FAILED for paymentId={}. " +
                            "Manual refund required. amount={}, reason={}, error={}",
                    razorpayPaymentId, amount, reason, e.getMessage(), e);
            return null; // never rethrow — must not break transaction save or saga compensation
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Notification — publishes to notification_queue
    // ─────────────────────────────────────────────────────────────

    private void sendRefundNotification(Transaction txn, String refundId) {
        try {
            String refundIdText = (refundId != null)
                    ? " Refund ID: " + refundId + "."
                    : "";

            String message = "Dear Customer, your payment of Rs." + txn.getAmount() +
                    " for transaction ID " + txn.getTransactionId() +
                    " has been refunded." + refundIdText +
                    " The amount will reflect in your account within 5-7 business days." +
                    " We apologize for the inconvenience.";

            NotificationEvent event = new NotificationEvent(
                    message,
                    txn.getUserEmail(),
                    txn.getUserContactNo(),
                    "PAYMENT_REFUND"
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY,
                    event
            );

            log.info("Refund notification published for transactionId={}, email={}",
                    txn.getTransactionId(), txn.getUserEmail());

        } catch (Exception e) {
            log.error("Failed to publish refund notification for transactionId={}: {}",
                    txn.getTransactionId(), e.getMessage());
        }
    }
}