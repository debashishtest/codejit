package com.codejit.auth.service;

import com.codejit.auth.entity.Role;
import com.codejit.auth.entity.User;
import com.codejit.auth.repository.UserRepository;
import com.codejit.common.dto.auth.AuthResponse;
import com.codejit.common.dto.auth.LoginRequest;
import com.codejit.common.dto.auth.RegisterRequest;
import com.codejit.common.dto.auth.UserDto;
import com.codejit.common.exception.BadRequestException;
import com.codejit.common.exception.ResourceNotFoundException;
import com.codejit.common.exception.UnauthorizedException;
import com.codejit.common.security.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${jwt.secret:" + JwtUtils.DEFAULT_SECRET + "}") String jwtSecret,
            @Value("${jwt.expiration:" + JwtUtils.DEFAULT_EXPIRATION_MS + "}") long jwtExpiration) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = new JwtUtils(jwtSecret, jwtExpiration);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail().trim())) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }

        Role role = Role.ROLE_INTERVIEWER;
        if (request.getRole() != null) {
            try {
                role = Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                role = Role.ROLE_INTERVIEWER;
            }
        }

        User user = User.builder()
                .name(request.getName() != null && !request.getName().isBlank() ? request.getName().trim() : request.getEmail().split("@")[0])
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        User saved = userRepository.save(user);
        String token = jwtUtils.generateToken(saved.getEmail(), saved.getRole().name(), saved.getId());

        return AuthResponse.builder()
                .token(token)
                .username(saved.getEmail())
                .email(saved.getEmail())
                .role(saved.getRole().name())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier();
        if (identifier == null || identifier.isBlank()) {
            throw new BadRequestException("Username or Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        User user = userRepository.findByEmailIgnoreCase(identifier.trim())
                .or(() -> userRepository.findByNameIgnoreCase(identifier.trim()))
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name(), user.getId());

        return AuthResponse.builder()
                .token(token)
                .username(user.getEmail())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Transactional(readOnly = true)
    public UserDto getProfile(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

