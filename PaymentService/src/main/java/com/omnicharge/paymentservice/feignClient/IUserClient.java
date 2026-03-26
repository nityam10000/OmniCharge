package com.omnicharge.paymentservice.feignClient;

import com.omnicharge.paymentservice.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "USERMANAGEMENT")
public interface IUserClient {

    // Uses GET /users/email/{email} — no @PreAuthorize on that endpoint,
    // so any authenticated request (with X-User-Role + X-User-Email headers) passes.
    // Avoids hitting GET /users/{userId} which requires ROLE_ADMIN.
    @GetMapping("/users/email/{email}")
    UserResponseDTO getUserByEmail(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Email") String email,
            @PathVariable("email") String targetEmail
    );
}