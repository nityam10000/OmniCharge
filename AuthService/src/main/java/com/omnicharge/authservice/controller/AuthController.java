package com.omnicharge.authservice.controller;

import com.omnicharge.authservice.dto.LoginRequestDTO;
import com.omnicharge.authservice.entity.UserEntity;
import com.omnicharge.authservice.repository.IUserRepository;
import com.omnicharge.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final IUserRepository userRepo;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        log.info("Login attempt for email: {}", request.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            log.info("Authentication successful for email: {}", request.getEmail());

            UserEntity user = userRepo.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        log.error("User not found after successful authentication for email: {}", request.getEmail());
                        return new RuntimeException("User not found");
                    });

            String token = jwtUtil.generateToken(
                    user.getEmail(),
                    user.getRole().name(),
                    user.getUserId()
            );
            
            log.info("JWT token generated successfully for user: {}", request.getEmail());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            log.error("Login failed for email: {}", request.getEmail(), e);
            throw e;
        }
    }
}