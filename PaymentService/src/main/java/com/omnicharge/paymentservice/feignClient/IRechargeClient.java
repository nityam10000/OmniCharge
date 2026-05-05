package com.omnicharge.paymentservice.feignClient;

import com.omnicharge.paymentservice.dto.RechargeResponseDTO;
import com.omnicharge.paymentservice.feignClient.fallback.IRechargeClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "RECHARGEPROCESSING", fallback = IRechargeClientFallback.class)
public interface IRechargeClient {

    @PutMapping("/recharge/{id}/status")
    ResponseEntity<String> updateRechargeStatus(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Email") String email,
            @PathVariable("id") Long id,
            @RequestParam("status") String status
    );

    @GetMapping("/recharge/{id}")
    RechargeResponseDTO getRechargeById(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Email") String email,
            @PathVariable("id") Long id
    );
}