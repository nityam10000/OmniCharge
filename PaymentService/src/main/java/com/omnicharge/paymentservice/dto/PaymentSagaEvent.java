package com.omnicharge.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Event published by PaymentService onto the saga exchange.
 * RechargeProcessing consumes it, updates its state, then
 * publishes it back (with eventType = "recharge.updated") as a reply.
 *
 * IMPORTANT: This same class (same fields, same package naming convention)
 * must be mirrored in RechargeProcessing under:
 *   com.omnicharge.rechargeprocessing.dto.PaymentSagaEvent
 * — Jackson deserialises by field name, so the package does not need to match.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSagaEvent {

    /** Unique saga correlation id — set to transactionId.toString() */
    private String sagaId;

    private UUID   transactionId;
    private Long   rechargeId;
    private Long   userId;
    private String userEmail;
    private String userContactNo;
    private Double amount;
    private String razorpayPaymentId;

    /**
     * Routing key / event type:
     *   "payment.completed"  — payment verified OK, update recharge to SUCCESS
     *   "payment.failed"     — signature invalid,   update recharge to FAILED
     *   "recharge.updated"   — reply from RechargeProcessing, saga step done
     */
    private String eventType;

    /** Populated only on failure paths */
    private String failureReason;
}