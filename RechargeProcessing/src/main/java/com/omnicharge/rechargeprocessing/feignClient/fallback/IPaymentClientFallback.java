package com.omnicharge.rechargeprocessing.feignClient.fallback;

import com.omnicharge.rechargeprocessing.dto.TransactionRequestDTO;
import com.omnicharge.rechargeprocessing.dto.TransactionResponseDTO;
import com.omnicharge.rechargeprocessing.feignClient.IPaymentClient;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class IPaymentClientFallback implements IPaymentClient {

    @Override
    public TransactionResponseDTO createTransaction(TransactionRequestDTO transactionRequestDTO) {
        throw new RuntimeException(
                "PaymentService is currently unavailable. " +
                        "Could not create transaction. Circuit Breaker is OPEN."
        );
    }

    @Override
    public List<TransactionResponseDTO> getAllTransactionsByUserId(Long userId) {
        // Safe fallback — return empty list instead of throwing
        return Collections.emptyList();
    }

    @Override
    public TransactionResponseDTO getTransactionByRechargeId(Long rechargeId) {
        throw new RuntimeException(
                "PaymentService is currently unavailable. " +
                        "Could not fetch transaction. Circuit Breaker is OPEN."
        );
    }
}