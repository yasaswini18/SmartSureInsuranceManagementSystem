package com.InsuranceManagementSystem.AuthService.controller;

import com.InsuranceManagementSystem.AuthService.config.TestSecurityConfig;
import com.InsuranceManagementSystem.AuthService.dtos.UserResponse;
import com.InsuranceManagementSystem.AuthService.repository.UserRepository;
import com.InsuranceManagementSystem.AuthService.security.JwtUtil;
import com.InsuranceManagementSystem.AuthService.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@Import(TestSecurityConfig.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("GET /api/auth/users → 200 OK")
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldReturn200() throws Exception {
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();

        when(authService.getAllUsers()).thenReturn(List.of(userResponse));

        mockMvc.perform(get("/api/auth/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Users fetched successfully"))
                .andExpect(jsonPath("$.data[0].email").value("test@example.com"));
    }

    @Test
    @DisplayName("GET /api/auth/users/{email} → 200 OK")
    @WithMockUser(roles = "ADMIN")
    void getUserByEmail_ShouldReturn200() throws Exception {
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .role("USER")
                .build();

        when(authService.getUserByEmail("test@example.com")).thenReturn(userResponse);

        mockMvc.perform(get("/api/auth/users/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User fetched successfully"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    @DisplayName("GET /api/auth/users/role/{role} → 200 OK")
    @WithMockUser(roles = "ADMIN")
    void getUsersByRole_ShouldReturn200() throws Exception {
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .role("USER")
                .build();

        when(authService.getUsersByRole("USER")).thenReturn(List.of(userResponse));

        mockMvc.perform(get("/api/auth/users/role/USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("USER users fetched successfully"))
                .andExpect(jsonPath("$.data[0].role").value("USER"));
    }

    @Test
    @DisplayName("GET /api/auth/users/count → 200 OK")
    @WithMockUser(roles = "ADMIN")
    void getTotalUserCount_ShouldReturn200() throws Exception {
        when(authService.getTotalUserCount()).thenReturn(5L);

        mockMvc.perform(get("/api/auth/users/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User count fetched successfully"))
                .andExpect(jsonPath("$.data").value(5));
    }


}
