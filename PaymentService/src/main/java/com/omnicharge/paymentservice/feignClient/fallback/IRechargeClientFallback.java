package com.omnicharge.paymentservice.feignClient.fallback;

import com.omnicharge.paymentservice.dto.RechargeResponseDTO;
import com.omnicharge.paymentservice.feignClient.IRechargeClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class IRechargeClientFallback implements IRechargeClient {

    @Override
    public ResponseEntity<String> updateRechargeStatus(String role, String email, Long id, String status) {
        throw new RuntimeException(
                "RechargeProcessing service is currently unavailable. " +
                        "Could not update recharge status. Circuit Breaker is OPEN."
        );
    }

    @Override
    public RechargeResponseDTO getRechargeById(String role, String email, Long id) {
        throw new RuntimeException(
                "RechargeProcessing service is currently unavailable. " +
                        "Could not fetch recharge details. Circuit Breaker is OPEN."
        );
    }
}