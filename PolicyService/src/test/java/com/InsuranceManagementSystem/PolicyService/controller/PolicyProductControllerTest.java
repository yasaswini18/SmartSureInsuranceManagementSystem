package com.InsuranceManagementSystem.PolicyService.controller;

import com.InsuranceManagementSystem.PolicyService.config.TestSecurityConfig;
import com.InsuranceManagementSystem.PolicyService.dto.PolicyProductRequest;
import com.InsuranceManagementSystem.PolicyService.dto.PolicyProductResponse;
import com.InsuranceManagementSystem.PolicyService.entity.PolicyType;
import com.InsuranceManagementSystem.PolicyService.security.JwtUtil;
import com.InsuranceManagementSystem.PolicyService.service.PolicyProductService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PolicyProductController.class)
@Import(TestSecurityConfig.class)
class PolicyProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PolicyProductService productService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private PolicyProductRequest request;
    private PolicyProductResponse response;

    @BeforeEach
    void setUp() {
        request = new PolicyProductRequest(
                "Health Shield", PolicyType.HEALTH, "Comprehensive health insurance coverage",
                new BigDecimal("5000"), new BigDecimal("500000"),
                12, 18, 65
        );
        response = PolicyProductResponse.builder()
                .id(1L).name("Health Shield").type(PolicyType.HEALTH)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("POST /api/policies/products -> 201")
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void createProduct() throws Exception {
        Mockito.when(productService.createProduct(any(), eq("admin@test.com"))).thenReturn(response);
        mockMvc.perform(post("/api/policies/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Health Shield"));
    }

    @Test
    @DisplayName("PUT /api/policies/products/1 -> 200")
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void updateProduct() throws Exception {
        Mockito.when(productService.updateProduct(eq(1L), any(), eq("admin@test.com"))).thenReturn(response);
        mockMvc.perform(put("/api/policies/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/policies/products/1 -> 200")
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void deactivateProduct() throws Exception {
        mockMvc.perform(delete("/api/policies/products/1"))
                .andExpect(status().isOk());
        Mockito.verify(productService).deactivateProduct(eq(1L), eq("admin@test.com"));
    }

    @Test
    @DisplayName("GET /api/policies/products -> 200")
    @WithMockUser(roles = "USER")
    void getAllActiveProducts() throws Exception {
        Mockito.when(productService.getAllActiveProducts()).thenReturn(List.of(response));
        mockMvc.perform(get("/api/policies/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Health Shield"));
    }

    @Test
    @DisplayName("GET /api/policies/products/1 -> 200")
    @WithMockUser(roles = "USER")
    void getProductById() throws Exception {
        Mockito.when(productService.getProductById(1L)).thenReturn(response);
        mockMvc.perform(get("/api/policies/products/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/policies/products/type/HEALTH -> 200")
    @WithMockUser(roles = "USER")
    void getProductsByType() throws Exception {
        Mockito.when(productService.getProductsByType(PolicyType.HEALTH)).thenReturn(List.of(response));
        mockMvc.perform(get("/api/policies/products/type/HEALTH"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/policies/products/1/reactivate -> 200")
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void reactivateProduct() throws Exception {
        mockMvc.perform(put("/api/policies/products/1/reactivate"))
                .andExpect(status().isOk());
        Mockito.verify(productService).reactivateProduct(eq(1L), eq("admin@test.com"));
    }
}
