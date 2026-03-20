package com.omnicharge.authservice.controller;

import com.omnicharge.authservice.dto.LoginRequestDTO;
import com.omnicharge.authservice.entity.UserEntity;
import com.omnicharge.authservice.repository.IUserRepository;
import com.omnicharge.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
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
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final IUserRepository userRepo;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserEntity user = userRepo.findByEmail(request.getEmail())
                .orElseThrow();

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getUserId()
        );

        return ResponseEntity.ok(Map.of("token", token));
    }
}