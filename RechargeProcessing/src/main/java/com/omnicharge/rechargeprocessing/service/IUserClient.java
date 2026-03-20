package com.omnicharge.rechargeprocessing.service;

import com.omnicharge.rechargeprocessing.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("USERMANAGEMENT")
@Service
public interface IUserClient {
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId);

    @GetMapping("/users/email/{email}")
    ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable String email);
}
