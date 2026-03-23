package com.omnicharge.rechargeprocessing.dto;


import com.omnicharge.rechargeprocessing.enums.PaymentMethod;
import com.omnicharge.rechargeprocessing.enums.RechargeStatus;
import com.omnicharge.rechargeprocessing.enums.TransactionStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequestDTO {
    private Double amount;
    private Long rechargeId;
    private TransactionStatus status;        // "PENDING"
    private PaymentMethod paymentMethod; // "UPI", "CARD", "NETBANKING"
}