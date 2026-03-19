package com.omnicharge.rechargeprocessing.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.omnicharge.rechargeprocessing.dto.UserResponseDTO;

@FeignClient(name = "user-service", url = "http://localhost:8081/users")
public interface IUserClient {
    @GetMapping("/{id}")
    UserResponseDTO getUserById(@PathVariable("id") Long id);
}
