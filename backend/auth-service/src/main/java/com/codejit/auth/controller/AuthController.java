package com.codejit.auth.controller;

import com.codejit.auth.service.AuthService;
import com.codejit.common.dto.auth.AuthResponse;
import com.codejit.common.dto.auth.LoginRequest;
import com.codejit.common.dto.auth.RegisterRequest;
import com.codejit.common.dto.auth.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/v1/public/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/api/v1/public/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/auth/me")
    public ResponseEntity<UserDto> getCurrentUser(@RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDto profile = authService.getProfile(userEmail);
        return ResponseEntity.ok(profile);
    }
}

