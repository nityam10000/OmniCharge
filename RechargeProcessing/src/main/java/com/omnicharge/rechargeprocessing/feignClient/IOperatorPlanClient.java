package com.omnicharge.rechargeprocessing.feignClient;

import com.omnicharge.rechargeprocessing.feignClient.fallback.IOperatorPlanClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.omnicharge.rechargeprocessing.dto.PlanResponseDTO;

@FeignClient(name = "OPERATORPLANMANAGEMENT", fallback = IOperatorPlanClientFallback.class)
public interface IOperatorPlanClient {

    @GetMapping("/plans/{id}")
    PlanResponseDTO getPlanById(@PathVariable("id") Long id);

    @GetMapping("/plans/operator/{operatorId}")
    java.util.List<PlanResponseDTO> getPlansByOperatorId(@PathVariable("operatorId") Long operatorId);
}
