package com.omnicharge.rechargeprocessing.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.omnicharge.rechargeprocessing.dto.PlanResponseDTO;

@FeignClient(name = "operator-plan-service", url = "http://localhost:8087/operatormanagement/plans")
public interface IOperatorPlanClient {

    @GetMapping("/{id}")
    PlanResponseDTO getPlanById(@PathVariable("id") Long id);
}
