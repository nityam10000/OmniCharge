package com.omnicharge.paymentservice.mapper;

import com.omnicharge.paymentservice.dto.TransactionRequestDTO;
import com.omnicharge.paymentservice.dto.TransactionResponseDTO;
import com.omnicharge.paymentservice.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class Mapper {

    public TransactionResponseDTO toTransactionResponseDTO(Transaction transaction) {
        return TransactionResponseDTO.builder()
                .id((long) Math.abs(transaction.getTransactionId().hashCode()))
                .rechargeId(transaction.getRechargeId())
                .userId(transaction.getUserId())
                .amount(transaction.getAmount())
                .paymentMethod(transaction.getPaymentMethod())
                .status(transaction.getStatus())
                .razorpayOrderId(transaction.getRazorpayOrderId())
                .razorpayPaymentId(transaction.getRazorpayPaymentId())
                .createdAt(transaction.getTimestamp())
                .build();
    }

}