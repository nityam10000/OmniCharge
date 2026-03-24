package com.omnicharge.paymentservice.feignClient;

import com.omnicharge.paymentservice.dto.PlanResponseDTO;
import com.omnicharge.paymentservice.feignClient.fallback.IOperatorPlanClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "OPERATORPLANMANAGEMENT", fallback = IOperatorPlanClientFallback.class)
public interface IOperatorPlanClient {

    @GetMapping("/plans/{id}")
    PlanResponseDTO getPlanById(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Email") String email,
            @PathVariable("id") Long id
    );
}