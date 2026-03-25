
package com.omnicharge.rechargeprocessing.feignClient;

import com.omnicharge.rechargeprocessing.feignClient.fallback.IUserClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.omnicharge.rechargeprocessing.dto.UserResponseDTO;

@FeignClient(name = "USERMANAGEMENT", fallback = IUserClientFallback.class)
public interface IUserClient {
    @GetMapping("/{id}")
    UserResponseDTO getUserById(@PathVariable("id") Long id);

}
