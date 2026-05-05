package com.omnicharge.rechargeprocessing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Mirror of com.omnicharge.paymentservice.dto.PaymentSagaEvent.
 * Jackson deserialises by field name — package does not need to match.
 * Keep fields in sync with the PaymentService version.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSagaEvent {

    private String sagaId;
    private UUID   transactionId;
    private Long   rechargeId;
    private Long   userId;
    private String userEmail;
    private String userContactNo;
    private Double amount;
    private String razorpayPaymentId;
    private String eventType;
    private String failureReason;
}