package com.codejit.auth;

import com.codejit.auth.entity.Role;
import com.codejit.auth.entity.User;
import com.codejit.auth.repository.UserRepository;
import com.codejit.auth.service.AuthService;
import com.codejit.common.dto.auth.AuthResponse;
import com.codejit.common.dto.auth.LoginRequest;
import com.codejit.common.dto.auth.RegisterRequest;
import com.codejit.common.exception.BadRequestException;
import com.codejit.common.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, "test-secret-key-at-least-256-bits-long-for-auth-testing-purposes!", 3600000);
    }

    @Test
    @DisplayName("Should register new user successfully")
    void testRegisterSuccess() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alice")
                .email("alice@codejit.io")
                .password("password123")
                .role("ROLE_INTERVIEWER")
                .build();

        when(userRepository.existsByEmailIgnoreCase("alice@codejit.io")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");

        User savedUser = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@codejit.io")
                .passwordHash("hashedPassword")
                .role(Role.ROLE_INTERVIEWER)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("alice@codejit.io", response.getEmail());
        assertEquals("ROLE_INTERVIEWER", response.getRole());
    }

    @Test
    @DisplayName("Should fail registration when email exists")
    void testRegisterDuplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .email("alice@codejit.io")
                .password("password123")
                .build();

        when(userRepository.existsByEmailIgnoreCase("alice@codejit.io")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() {
        LoginRequest request = LoginRequest.builder()
                .email("alice@codejit.io")
                .password("password123")
                .build();

        User user = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@codejit.io")
                .passwordHash("hashedPassword")
                .role(Role.ROLE_INTERVIEWER)
                .build();

        when(userRepository.findByEmailIgnoreCase("alice@codejit.io")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("alice@codejit.io", response.getEmail());
    }

    @Test
    @DisplayName("Should reject login with invalid password")
    void testLoginInvalidPassword() {
        LoginRequest request = LoginRequest.builder()
                .email("alice@codejit.io")
                .password("wrongpassword")
                .build();

        User user = User.builder()
                .id(1L)
                .email("alice@codejit.io")
                .passwordHash("hashedPassword")
                .role(Role.ROLE_INTERVIEWER)
                .build();

        when(userRepository.findByEmailIgnoreCase("alice@codejit.io")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }
}

