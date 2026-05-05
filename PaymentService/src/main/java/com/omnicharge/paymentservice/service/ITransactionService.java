package com.omnicharge.paymentservice.service;

import com.omnicharge.paymentservice.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ITransactionService {
    public TransactionResponseDTO createTransaction(TransactionRequestDTO transactionRequestDTO);
    public List<TransactionResponseDTO> getAllTransactionsByUserId(Long userId);
    public TransactionResponseDTO getTransactionByRechargeId(Long rechargeId) ;
    public Page<TransactionResponseDTO> getMyTransactions(Pageable pageable);
    RazorpayOrderResponseDTO createOrder(RazorpayOrderRequestDTO dto);
    TransactionResponseDTO verifyPayment(PaymentVerifyRequestDTO dto);
}
