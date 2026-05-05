package com.omnicharge.paymentservice.dto;

import com.omnicharge.paymentservice.enums.TransactionStatus;
import com.omnicharge.paymentservice.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder

public class TransactionResponseDTO {
    private Long id;
    private Long rechargeId;
    private Long userId;
    private Double amount;
    private PaymentMethod paymentMethod;
    private TransactionStatus status;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private LocalDateTime createdAt;
}