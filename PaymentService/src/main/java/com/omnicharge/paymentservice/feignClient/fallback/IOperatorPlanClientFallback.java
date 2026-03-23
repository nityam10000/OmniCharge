package com.omnicharge.paymentservice.feignClient.fallback;



import com.omnicharge.paymentservice.dto.PlanResponseDTO;
import com.omnicharge.paymentservice.feignClient.IOperatorPlanClient;
import org.springframework.stereotype.Component;

@Component
public class IOperatorPlanClientFallback implements IOperatorPlanClient {

    @Override
    public PlanResponseDTO getPlanById(String role, String email, Long id) {
        throw new RuntimeException(
                "OperatorPlanManagement service is currently unavailable. " +
                        "Circuit Breaker is OPEN. Please try again later."
        );
    }
}