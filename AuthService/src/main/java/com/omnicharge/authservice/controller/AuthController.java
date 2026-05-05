package com.omnicharge.authservice.controller;
import com.omnicharge.authservice.dto.EmailRequestDTO;
import com.omnicharge.authservice.dto.LoginRequestDTO;
import com.omnicharge.authservice.dto.LoginResponseDTO;
import com.omnicharge.authservice.dto.RefreshTokenRequestDTO;
import com.omnicharge.authservice.dto.ResetPasswordRequestDTO;
import com.omnicharge.authservice.dto.VerifyOtpRequestDTO;
import com.omnicharge.authservice.entity.UserEntity;
import com.omnicharge.authservice.exception.UserNotFoundException;
import com.omnicharge.authservice.repository.IUserRepository;
import com.omnicharge.authservice.service.OtpService;
import com.omnicharge.authservice.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final IUserRepository userRepo;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;

    //PASSWORD LOGIN
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

            LoginResponseDTO tokens = createAndStoreTokens(user);
            log.info("JWT token generated successfully for user: {}", request.getEmail());
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            log.error("Login failed for email: {}", request.getEmail(), e);
            throw e;
        }
    }


    //  OTP LOGIN  (send → verify → JWT)
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody EmailRequestDTO request) {
        if (!userRepo.existsByEmail(request.getEmail())) {
            throw new UserNotFoundException("No account found for " + request.getEmail());
        }
        otpService.sendOtp(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "OTP sent to " + request.getEmail()));
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequestDTO request) {
        if (!otpService.verifyOtp(request.getEmail(), request.getOtp())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired OTP"));
        }
        UserEntity user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + request.getEmail()));
        return ResponseEntity.ok(createAndStoreTokens(user));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        String refreshToken = request.getRefreshToken();
        String email = jwtUtil.extractEmail(refreshToken);
        String tokenType = jwtUtil.extractTokenType(refreshToken);

        if (!"refresh".equals(tokenType)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid refresh token"));
        }

        UserEntity user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        boolean refreshTokenMismatch = user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken);
        boolean refreshTokenExpired = user.getRefreshTokenExpiry() == null
                || user.getRefreshTokenExpiry().isBefore(LocalDateTime.now());

        if (refreshTokenMismatch || refreshTokenExpired) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token is invalid or expired"));
        }

        return ResponseEntity.ok(createAndStoreTokens(user));
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody EmailRequestDTO request) {
        if (!userRepo.existsByEmail(request.getEmail())) {
            throw new UserNotFoundException("No account found for " + request.getEmail());
        }
        otpService.sendForgotPasswordOtp(request.getEmail());
        log.info("Forgot-password OTP triggered for {}", request.getEmail());
        return ResponseEntity.ok(Map.of("message",
                "Password-reset OTP sent to " + request.getEmail() + ". Valid for 5 minutes."));
    }


    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        if (!otpService.verifyForgotPasswordOtp(request.getEmail(), request.getOtp())) {
            log.warn("Invalid/expired forgot-password OTP for {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired OTP"));
        }

        UserEntity user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + request.getEmail()));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);

        log.info("Password reset successfully for {}", request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Password updated successfully. Please log in."));
    }

    private LoginResponseDTO createAndStoreTokens(UserEntity user) {
        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                user.getRole().name(),
                user.getUserId()
        );
        String refreshToken = jwtUtil.generateRefreshToken(
                user.getEmail(),
                user.getRole().name(),
                user.getUserId()
        );

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now()
                .plusSeconds(jwtUtil.getRefreshTokenExpirationMs() / 1000));
        userRepo.save(user);

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
