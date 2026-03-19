package com.omnicharge.paymentservice.mapper;

import com.omnicharge.paymentservice.dto.TransactionRequestDTO;
import com.omnicharge.paymentservice.dto.TransactionResponseDTO;
import com.omnicharge.paymentservice.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class Mapper {

    public TransactionResponseDTO toTransactionResponseDTO(Transaction transaction) {
        TransactionResponseDTO transactionResponseDTO = new TransactionResponseDTO();
        transactionResponseDTO.setAmount(transaction.getAmount());
        transactionResponseDTO.setPaymentMethod(transaction.getPaymentMethod());
        transactionResponseDTO.setTimestamp(transaction.getTimestamp());
        transactionResponseDTO.setTransactionStatus(transaction.getStatus());
        return transactionResponseDTO;
    }

    public Transaction toTransaction(TransactionRequestDTO transactionRequestDTO) {
        Transaction transaction = new Transaction();
        transaction.setAmount(transactionRequestDTO.getAmount());
        transaction.setPaymentMethod(transactionRequestDTO.getPaymentMethod());
        transaction.setRechargeId(transactionRequestDTO.getRechargeId());
        transaction.setStatus(transactionRequestDTO.getStatus());
        transaction.setUserId(transactionRequestDTO.getUserId());
        return transaction;
    }
}