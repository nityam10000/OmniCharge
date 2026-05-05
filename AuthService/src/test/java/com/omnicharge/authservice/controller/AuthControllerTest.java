package com.omnicharge.authservice.controller;

import com.omnicharge.authservice.dto.*;
import com.omnicharge.authservice.entity.UserEntity;
import com.omnicharge.authservice.enums.Roles;
import com.omnicharge.authservice.exception.UserNotFoundException;
import com.omnicharge.authservice.repository.IUserRepository;
import com.omnicharge.authservice.service.OtpService;
import com.omnicharge.authservice.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private IUserRepository userRepo;
    @Mock
    private OtpService otpService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setEmail("test@test.com");
        user.setRole(Roles.USER);
        user.setUserId(1L);
        user.setRefreshToken("old-refresh-token");
        user.setRefreshTokenExpiry(LocalDateTime.now().plusHours(1));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnTokens() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("test@test.com");
        request.setPassword("password");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(anyString(), anyString(), anyLong())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(anyString(), anyString(), anyLong())).thenReturn("refresh-token");
        when(jwtUtil.getRefreshTokenExpirationMs()).thenReturn(3600000L);

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void sendOtp_WithValidEmail_ShouldReturnOk() {
        EmailRequestDTO request = new EmailRequestDTO();
        request.setEmail("test@test.com");

        when(userRepo.existsByEmail(anyString())).thenReturn(true);

        ResponseEntity<?> response = authController.sendOtp(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(otpService).sendOtp("test@test.com");
    }

    @Test
    void verifyOtp_WithValidOtp_ShouldReturnTokens() {
        VerifyOtpRequestDTO request = new VerifyOtpRequestDTO();
        request.setEmail("test@test.com");
        request.setOtp("123456");

        when(otpService.verifyOtp(anyString(), anyString())).thenReturn(true);
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(anyString(), anyString(), anyLong())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(anyString(), anyString(), anyLong())).thenReturn("refresh-token");
        when(jwtUtil.getRefreshTokenExpirationMs()).thenReturn(3600000L);

        ResponseEntity<?> response = authController.verifyOtp(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void verifyOtp_WithInvalidOtp_ShouldReturnUnauthorized() {
        VerifyOtpRequestDTO request = new VerifyOtpRequestDTO();
        request.setEmail("test@test.com");
        request.setOtp("123456");

        when(otpService.verifyOtp(anyString(), anyString())).thenReturn(false);

        ResponseEntity<?> response = authController.verifyOtp(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewTokens() {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
        request.setRefreshToken("old-refresh-token");

        when(jwtUtil.extractEmail(anyString())).thenReturn("test@test.com");
        when(jwtUtil.extractTokenType(anyString())).thenReturn("refresh");
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(anyString(), anyString(), anyLong())).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken(anyString(), anyString(), anyLong())).thenReturn("new-refresh-token");
        when(jwtUtil.getRefreshTokenExpirationMs()).thenReturn(3600000L);

        ResponseEntity<?> response = authController.refreshToken(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void forgotPassword_WithValidEmail_ShouldReturnOk() {
        EmailRequestDTO request = new EmailRequestDTO();
        request.setEmail("test@test.com");

        when(userRepo.existsByEmail(anyString())).thenReturn(true);

        ResponseEntity<?> response = authController.forgotPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(otpService).sendForgotPasswordOtp("test@test.com");
    }

    @Test
    void resetPassword_WithValidOtp_ShouldReturnOk() {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setEmail("test@test.com");
        request.setOtp("123456");
        request.setNewPassword("new-password");

        when(otpService.verifyForgotPasswordOtp(anyString(), anyString())).thenReturn(true);
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        ResponseEntity<?> response = authController.resetPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepo).save(user);
    }

    @Test
    void sendOtp_WithUserNotFound_ShouldThrowException() {
        EmailRequestDTO request = new EmailRequestDTO();
        request.setEmail("notfound@test.com");

        when(userRepo.existsByEmail(anyString())).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> authController.sendOtp(request));
    }

    @Test
    void forgotPassword_WithUserNotFound_ShouldThrowException() {
        EmailRequestDTO request = new EmailRequestDTO();
        request.setEmail("notfound@test.com");

        when(userRepo.existsByEmail(anyString())).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> authController.forgotPassword(request));
    }
}
