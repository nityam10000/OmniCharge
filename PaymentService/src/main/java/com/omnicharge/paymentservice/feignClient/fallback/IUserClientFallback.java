package com.omnicharge.paymentservice.feignClient.fallback;

import com.omnicharge.paymentservice.dto.UserResponseDTO;
import com.omnicharge.paymentservice.exception.ServiceUnavailableException;
import com.omnicharge.paymentservice.feignClient.IUserClient;
import org.springframework.stereotype.Component;

@Component
public class IUserClientFallback implements IUserClient {

    public UserResponseDTO getUserById(Long id) {
        throw new ServiceUnavailableException(
                "UserManagement service is currently unavailable. " +
                        "Could not fetch user details. Circuit Breaker is OPEN."
        );
    }

    @Override
    public UserResponseDTO getUserByEmail(String role, String email, String targetEmail) {
        throw new ServiceUnavailableException(
                "UserManagement service is currently unavailable. " +
                        "Could not fetch user details. Circuit Breaker is OPEN."
        );
    }
}