package com.omnicharge.paymentservice.service;

import com.omnicharge.paymentservice.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ITransactionService {
    TransactionResponseDTO createTransaction(TransactionRequestDTO transactionRequestDTO);
    List<TransactionResponseDTO> getAllTransactionsByUserId(Long userId);
    TransactionResponseDTO getTransactionByRechargeId(Long rechargeId);
    Page<TransactionResponseDTO> getMyTransactions(Pageable pageable);
    TransactionResponseDTO processPayment(CustomPaymentRequestDTO dto);
}
