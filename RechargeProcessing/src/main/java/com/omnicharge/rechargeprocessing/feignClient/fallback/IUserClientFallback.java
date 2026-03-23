package com.omnicharge.rechargeprocessing.feignClient.fallback;

import com.omnicharge.rechargeprocessing.dto.UserResponseDTO;
import com.omnicharge.rechargeprocessing.feignClient.IUserClient;
import org.springframework.stereotype.Component;

@Component
public class IUserClientFallback implements IUserClient {

    @Override
    public UserResponseDTO getUserById(Long id) {
        throw new RuntimeException(
                "UserManagement service is currently unavailable. " +
                        "Could not fetch user details. Circuit Breaker is OPEN."
        );
    }
}