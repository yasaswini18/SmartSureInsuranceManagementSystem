package com.InsuranceManagementSystem.PolicyService.controller;

import com.InsuranceManagementSystem.PolicyService.config.TestSecurityConfig;
import com.InsuranceManagementSystem.PolicyService.dto.PolicyValidationResponse;
import com.InsuranceManagementSystem.PolicyService.dto.PurchasePolicyRequest;
import com.InsuranceManagementSystem.PolicyService.dto.PurchasedPolicyResponse;
import com.InsuranceManagementSystem.PolicyService.security.JwtUtil;
import com.InsuranceManagementSystem.PolicyService.service.PurchasedPolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PurchasedPolicyController.class)
@Import(TestSecurityConfig.class)
class PurchasedPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PurchasedPolicyService purchasedPolicyService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private PurchasePolicyRequest request;
    private PurchasedPolicyResponse response;

    @BeforeEach
    void setUp() {
        request = new PurchasePolicyRequest(1L, null, 30, null);
        response = PurchasedPolicyResponse.builder()
                .id(100L).policyNumber("POL-123").customerEmail("test@test.com")
                .build();
    }

    @Test
    @DisplayName("GET /api/policies/health -> 200")
    void health() throws Exception {
        mockMvc.perform(get("/api/policies/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/policies/purchase -> 201")
    @WithMockUser(username = "test@test.com", roles = "USER")
    void purchasePolicy() throws Exception {
        Mockito.when(purchasedPolicyService.purchasePolicy(any(), eq("test@test.com"))).thenReturn(response);
        mockMvc.perform(post("/api/policies/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.policyNumber").value("POL-123"));
    }

    @Test
    @DisplayName("GET /api/policies/my-policies -> 200")
    @WithMockUser(username = "test@test.com", roles = "USER")
    void getMyPolicies() throws Exception {
        Mockito.when(purchasedPolicyService.getMyPolicies("test@test.com")).thenReturn(List.of(response));
        mockMvc.perform(get("/api/policies/my-policies"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/policies/100 -> 200")
    @WithMockUser(username = "test@test.com", roles = "USER")
    void getPolicyById() throws Exception {
        Mockito.when(purchasedPolicyService.getPolicyById(eq(100L), eq("test@test.com"), eq("USER"))).thenReturn(response);
        mockMvc.perform(get("/api/policies/100"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/policies/all -> 200")
    @WithMockUser(roles = "ADMIN")
    void getAllPolicies() throws Exception {
        Mockito.when(purchasedPolicyService.getAllPolicies()).thenReturn(List.of(response));
        mockMvc.perform(get("/api/policies/all"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/policies/100/cancel -> 200")
    @WithMockUser(username = "test@test.com", roles = "USER")
    void cancelPolicy() throws Exception {
        Mockito.when(purchasedPolicyService.cancelPolicy(eq(100L), eq("test@test.com"))).thenReturn(response);
        mockMvc.perform(put("/api/policies/100/cancel"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/policies/validate -> 200")
    void validatePolicy() throws Exception {
        PolicyValidationResponse valRes = new PolicyValidationResponse(true, "POL-123", "test@test.com", "Name", "HEALTH", null, null, "ACTIVE", "valid");
        Mockito.when(purchasedPolicyService.validatePolicy("POL-123", "test@test.com")).thenReturn(valRes);
        mockMvc.perform(get("/api/policies/validate")
                .param("policyNumber", "POL-123")
                .param("customerEmail", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }
}
