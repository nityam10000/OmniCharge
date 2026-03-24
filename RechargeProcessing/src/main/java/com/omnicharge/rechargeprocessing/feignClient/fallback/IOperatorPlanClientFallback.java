
package com.omnicharge.rechargeprocessing.feignClient.fallback;

import com.omnicharge.rechargeprocessing.dto.PlanResponseDTO;
import com.omnicharge.rechargeprocessing.feignClient.IOperatorPlanClient;
import org.springframework.stereotype.Component;

@Component
public class IOperatorPlanClientFallback implements IOperatorPlanClient {

    @Override
    public PlanResponseDTO getPlanById(Long id) {
        throw new RuntimeException(
                "OperatorPlanManagement service is currently unavailable. " +
                        "Could not fetch plan details. Circuit Breaker is OPEN."
        );
    }
}