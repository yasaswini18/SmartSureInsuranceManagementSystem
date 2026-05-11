package com.InsuranceManagementSystem.AdminService.controller;

import com.InsuranceManagementSystem.AdminService.dto.external.UserResponse;
import com.InsuranceManagementSystem.AdminService.security.JwtAuthFilter;
import com.InsuranceManagementSystem.AdminService.security.JwtUtil;
import com.InsuranceManagementSystem.AdminService.service.AdminUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminUserService adminUserService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin@insurance.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setFullName("Test User");
        userResponse.setEmail("test@user.com");
        userResponse.setRole("USER");
    }

    @Test
    @DisplayName("GET /api/admin/users -> 200 OK")
    void getAllUsers_ShouldReturn200() throws Exception {
        when(adminUserService.getAllUsers(anyString(), anyString())).thenReturn(List.of(userResponse));

        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].email").value("test@user.com"));
    }

    @Test
    @DisplayName("GET /api/admin/users/{email} -> 200 OK")
    void getUserByEmail_ShouldReturn200() throws Exception {
        when(adminUserService.getUserByEmail(anyString(), anyString(), anyString())).thenReturn(userResponse);

        mockMvc.perform(get("/api/admin/users/test@user.com")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@user.com"));
    }

    @Test
    @DisplayName("GET /api/admin/users/role/{role} -> 200 OK")
    void getUsersByRole_ShouldReturn200() throws Exception {
        when(adminUserService.getUsersByRole(anyString(), anyString(), anyString())).thenReturn(List.of(userResponse));

        mockMvc.perform(get("/api/admin/users/role/USER")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].email").value("test@user.com"));
    }

    @Test
    @DisplayName("GET /api/admin/users/count -> 200 OK")
    void getTotalUserCount_ShouldReturn200() throws Exception {
        when(adminUserService.getTotalUserCount(anyString(), anyString())).thenReturn(100L);

        mockMvc.perform(get("/api/admin/users/count")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(100));
    }
}
