package com.omnicharge.paymentservice.service;

import com.omnicharge.paymentservice.dto.*;

import java.util.List;

public interface ITransactionService {
    public TransactionResponseDTO createTransaction(TransactionRequestDTO transactionRequestDTO);
    public List<TransactionResponseDTO> getAllTransactionsByUserId(Long userId);
    public TransactionResponseDTO getTransactionByRechargeId(Long rechargeId) ;
    public List<TransactionResponseDTO> getMyTransactions();
    RazorpayOrderResponseDTO createOrder(RazorpayOrderRequestDTO dto);
    TransactionResponseDTO verifyPayment(PaymentVerifyRequestDTO dto);
}
