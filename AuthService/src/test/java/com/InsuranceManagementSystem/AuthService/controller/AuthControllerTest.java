package com.InsuranceManagementSystem.AuthService.controller;

import com.InsuranceManagementSystem.AuthService.config.TestSecurityConfig;
import com.InsuranceManagementSystem.AuthService.dtos.AuthResponse;
import com.InsuranceManagementSystem.AuthService.dtos.LoginRequest;
import com.InsuranceManagementSystem.AuthService.dtos.RegisterRequest;
import com.InsuranceManagementSystem.AuthService.dtos.UpdateUserRoleRequest;
import com.InsuranceManagementSystem.AuthService.repository.UserRepository;
import com.InsuranceManagementSystem.AuthService.security.JwtUtil;
import com.InsuranceManagementSystem.AuthService.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("POST /api/auth/register → 201 Created")
    void register_WithValidRequest_ShouldReturn201() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "John Customer", "john@gmail.com", "John@123", "9876543210", "Hyderabad"
        );

        AuthResponse mockResponse = AuthResponse.builder()
                .message("User registered successfully")
                .userId(1L)
                .build();

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("POST /api/auth/register → 400 when email blank")
    void register_WithBlankEmail_ShouldReturn400() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "John", "", "John@123", "9876543210", "Hyderabad"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register → 400 when password too short")
    void register_WithShortPassword_ShouldReturn400() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "John", "john@gmail.com", "abc", "9876543210", "Hyderabad"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login → 200 OK")
    void login_WithValidCredentials_ShouldReturn200() throws Exception {
        LoginRequest request = new LoginRequest(
                "john@gmail.com", "John@123"
        );

        AuthResponse mockResponse = AuthResponse.builder()
                .accessToken("mock.jwt.token")
                .refreshToken("mock.refresh.token")
                .email("john@gmail.com")
                .role("USER")
                .userId(1L)
                .name("John Customer")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock.jwt.token"))
                .andExpect(jsonPath("$.email").value("john@gmail.com"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.name").value("John Customer"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("GET /api/auth/health → 200 OK")
    void health_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/auth/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/auth/refresh → 200 OK")
    void refresh_WithValidToken_ShouldReturn200() throws Exception {
        AuthResponse mockResponse = AuthResponse.builder()
                .accessToken("new.jwt.token")
                .refreshToken("new.refresh.token")
                .build();

        when(authService.refreshAccessToken("valid.refresh.token"))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/refresh")
                .cookie(new Cookie("refreshToken", "valid.refresh.token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new.jwt.token"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("POST /api/auth/logout → 200 OK")
    void logout_WithValidToken_ShouldReturn200() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .cookie(new Cookie("refreshToken", "valid.refresh.token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"))
                .andExpect(header().exists("Set-Cookie"));
                
        verify(authService).logout("valid.refresh.token");
    }

    @Test
    @DisplayName("POST /api/auth/create-admin → 201 Created")
    @WithMockUser(roles = "ADMIN")
    void createAdmin_WithValidRequest_ShouldReturn201() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Admin User", "admin@gmail.com", "Admin@123", "9876543210", "Hyderabad"
        );

        AuthResponse mockResponse = AuthResponse.builder()
                .message("Admin created successfully")
                .userId(2L)
                .refreshToken("admin.refresh.token")
                .build();

        when(authService.createAdmin(any(RegisterRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/create-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Admin created successfully"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("GET /api/auth/validate → 200 OK")
    void validate_WithValidToken_ShouldReturn200() throws Exception {
        when(authService.validateAndExtractEmail("mock.token")).thenReturn("john@gmail.com");
        when(jwtUtil.extractRole("mock.token")).thenReturn("USER");
        when(jwtUtil.extractUserId("mock.token")).thenReturn(1L);

        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer mock.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Token is valid"))
                .andExpect(jsonPath("$.data.email").value("john@gmail.com"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.userId").value(1));
    }

    @Test
    @DisplayName("PATCH /api/auth/{email}/role → 200 OK")
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_WithValidRequest_ShouldReturn200() throws Exception {
        UpdateUserRoleRequest request = new UpdateUserRoleRequest();
        request.setRole("ADMIN");

        mockMvc.perform(patch("/api/auth/john@gmail.com/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User role updated successfully"));

        verify(authService).updateUserRole("john@gmail.com", "ADMIN");
    }
}
