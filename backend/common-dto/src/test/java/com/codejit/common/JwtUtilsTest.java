package com.codejit.common;

import com.codejit.common.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils("test-secret-key-that-is-at-least-256-bits-long-for-testing-purpose!", 3600000);
    }

    @Test
    @DisplayName("Should generate and validate JWT token successfully")
    void testGenerateAndValidateToken() {
        String token = jwtUtils.generateToken("developer@codejit.io", "ROLE_INTERVIEWER", 101L);
        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));
        assertEquals("developer@codejit.io", jwtUtils.extractUsername(token));
        assertEquals("ROLE_INTERVIEWER", jwtUtils.extractRole(token));
        assertEquals(101L, jwtUtils.extractUserId(token));
    }

    @Test
    @DisplayName("Should reject invalid token")
    void testInvalidToken() {
        assertFalse(jwtUtils.validateToken("invalid.token.signature"));
    }
}

