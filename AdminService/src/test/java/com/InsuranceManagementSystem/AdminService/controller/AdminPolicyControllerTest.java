package com.InsuranceManagementSystem.AdminService.controller;

import com.InsuranceManagementSystem.AdminService.dto.external.PolicyProductRequest;
import com.InsuranceManagementSystem.AdminService.dto.external.PolicyProductResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.PurchasedPolicyResponse;
import com.InsuranceManagementSystem.AdminService.security.JwtAuthFilter;
import com.InsuranceManagementSystem.AdminService.security.JwtUtil;
import com.InsuranceManagementSystem.AdminService.service.AdminPolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminPolicyController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminPolicyService adminPolicyService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private PolicyProductRequest request;
    private PolicyProductResponse response;
    private PurchasedPolicyResponse purchasedResponse;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin@insurance.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        request = new PolicyProductRequest();
        request.setName("Test Product");
        request.setDescription("Description");
        request.setType("HEALTH");
        request.setBasePremium(new BigDecimal("10000"));
        request.setCoverageAmount(new BigDecimal("100"));

        response = new PolicyProductResponse();
        response.setId(1L);
        response.setName("Test Product");
        response.setIsActive(true);

        purchasedResponse = new PurchasedPolicyResponse();
        purchasedResponse.setId(1L);
        purchasedResponse.setPolicyNumber("POL-123");
    }

    @Test
    @DisplayName("POST /api/admin/policies -> 201 Created")
    void createProduct_ShouldReturn201() throws Exception {
        when(adminPolicyService.createProduct(any(), anyString(), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/admin/policies")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Product"));
    }

    @Test
    @DisplayName("PUT /api/admin/policies/{productId} -> 200 OK")
    void updateProduct_ShouldReturn200() throws Exception {
        when(adminPolicyService.updateProduct(eq(1L), any(), anyString(), anyString())).thenReturn(response);

        mockMvc.perform(put("/api/admin/policies/1")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Product"));
    }

    @Test
    @DisplayName("DELETE /api/admin/policies/{productId} -> 200 OK")
    void deactivateProduct_ShouldReturn200() throws Exception {
        doNothing().when(adminPolicyService).deactivateProduct(eq(1L), anyString(), anyString());

        mockMvc.perform(delete("/api/admin/policies/1")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Policy product deactivated successfully"));
    }

    @Test
    @DisplayName("PUT /api/admin/policies/{productId}/reactivate -> 200 OK")
    void reactivateProduct_ShouldReturn200() throws Exception {
        doNothing().when(adminPolicyService).reactivateProduct(eq(1L), anyString(), anyString());

        mockMvc.perform(put("/api/admin/policies/1/reactivate")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Policy product reactivated successfully"));
    }

    @Test
    @DisplayName("GET /api/admin/policies -> 200 OK")
    void getAllProducts_ShouldReturn200() throws Exception {
        when(adminPolicyService.getAllProducts(anyString(), anyString())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/admin/policies")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Test Product"));
    }

    @Test
    @DisplayName("GET /api/admin/policies/{productId} -> 200 OK")
    void getProductById_ShouldReturn200() throws Exception {
        when(adminPolicyService.getProductById(eq(1L), anyString(), anyString())).thenReturn(response);

        mockMvc.perform(get("/api/admin/policies/1")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Product"));
    }

    @Test
    @DisplayName("GET /api/admin/policies/purchased -> 200 OK")
    void getAllPurchasedPolicies_ShouldReturn200() throws Exception {
        when(adminPolicyService.getAllPurchasedPolicies(anyString(), anyString())).thenReturn(List.of(purchasedResponse));

        mockMvc.perform(get("/api/admin/policies/purchased")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].policyNumber").value("POL-123"));
    }
}
