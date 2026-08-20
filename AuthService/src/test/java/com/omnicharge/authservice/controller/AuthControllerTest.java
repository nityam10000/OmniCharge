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
}
