package com.omnicharge.paymentservice.service;

import com.omnicharge.paymentservice.dto.TransactionRequestDTO;
import com.omnicharge.paymentservice.dto.TransactionResponseDTO;

import java.util.List;

public interface ITransactionService {
    public TransactionResponseDTO createTransaction(TransactionRequestDTO transactionRequestDTO);
    public List<TransactionResponseDTO> getAllTransactionsByUserId(Long userId);
    public TransactionResponseDTO getTransactionByRechargeId(Long rechargeId) ;
}
