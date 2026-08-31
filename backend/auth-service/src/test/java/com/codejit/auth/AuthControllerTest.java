package com.codejit.auth;

import com.codejit.auth.controller.AuthController;
import com.codejit.auth.service.AuthService;
import com.codejit.common.dto.auth.AuthResponse;
import com.codejit.common.dto.auth.LoginRequest;
import com.codejit.common.dto.auth.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/v1/public/register should return 201 Created")
    void testRegisterEndpoint() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alice")
                .email("alice@codejit.io")
                .password("password123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-test-token")
                .username("alice@codejit.io")
                .email("alice@codejit.io")
                .role("ROLE_INTERVIEWER")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/public/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-test-token"))
                .andExpect(jsonPath("$.username").value("alice@codejit.io"));
    }

    @Test
    @DisplayName("POST /api/v1/public/login should return 200 OK")
    void testLoginEndpoint() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("alice@codejit.io")
                .password("password123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-test-token")
                .username("alice@codejit.io")
                .email("alice@codejit.io")
                .role("ROLE_INTERVIEWER")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-test-token"));
    }
}

