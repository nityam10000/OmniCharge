package com.omnicharge.rechargeprocessing.service;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@EnableFeignClients("USERMANAGEMENT")
public interface IUserClient {
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId);
}
