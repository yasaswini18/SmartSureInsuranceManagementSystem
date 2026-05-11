package com.InsuranceManagementSystem.AdminService.controller;

import com.InsuranceManagementSystem.AdminService.dto.external.ClaimResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.ClaimReviewRequest;
import com.InsuranceManagementSystem.AdminService.dto.external.ClaimSummaryResponse;
import com.InsuranceManagementSystem.AdminService.security.JwtAuthFilter;
import com.InsuranceManagementSystem.AdminService.security.JwtUtil;
import com.InsuranceManagementSystem.AdminService.service.AdminClaimsService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminClaimsController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for pure controller testing
class AdminClaimsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminClaimsService adminClaimsService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private ClaimSummaryResponse summaryResponse;
    private ClaimResponse claimResponse;
    private ClaimReviewRequest reviewRequest;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin@insurance.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        summaryResponse = new ClaimSummaryResponse();
        summaryResponse.setId(1L);
        summaryResponse.setClaimNumber("CLM-123");
        summaryResponse.setStatus("PENDING");

        claimResponse = new ClaimResponse();
        claimResponse.setId(1L);
        claimResponse.setClaimNumber("CLM-123");
        claimResponse.setStatus("APPROVED");

        reviewRequest = new ClaimReviewRequest("APPROVED", "All looks good", new BigDecimal("1000"));
    }

    @Test
    @DisplayName("GET /api/admin/claims -> 200 OK")
    void getAllClaims_ShouldReturn200() throws Exception {
        when(adminClaimsService.getAllClaims(anyString(), anyString())).thenReturn(List.of(summaryResponse));

        mockMvc.perform(get("/api/admin/claims")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].claimNumber").value("CLM-123"));
    }

    @Test
    @DisplayName("GET /api/admin/claims/pending -> 200 OK")
    void getPendingClaims_ShouldReturn200() throws Exception {
        when(adminClaimsService.getPendingClaims(anyString(), anyString())).thenReturn(List.of(summaryResponse));

        mockMvc.perform(get("/api/admin/claims/pending")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].claimNumber").value("CLM-123"));
    }

    @Test
    @DisplayName("GET /api/admin/claims/{claimId} -> 200 OK")
    void getClaimById_ShouldReturn200() throws Exception {
        when(adminClaimsService.getClaimById(eq(1L), anyString(), anyString())).thenReturn(claimResponse);

        mockMvc.perform(get("/api/admin/claims/1")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.claimNumber").value("CLM-123"));
    }

    @Test
    @DisplayName("PUT /api/admin/claims/{claimId}/start-review -> 200 OK")
    void startReview_ShouldReturn200() throws Exception {
        when(adminClaimsService.startReview(eq(1L), anyString(), anyString())).thenReturn(claimResponse);

        mockMvc.perform(put("/api/admin/claims/1/start-review")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.claimNumber").value("CLM-123"));
    }

    @Test
    @DisplayName("PUT /api/admin/claims/{claimId}/review -> 200 OK")
    void reviewClaim_ShouldReturn200() throws Exception {
        when(adminClaimsService.reviewClaim(eq(1L), any(), anyString(), anyString())).thenReturn(claimResponse);

        mockMvc.perform(put("/api/admin/claims/1/review")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Claim approved successfully"))
                .andExpect(jsonPath("$.data.claimNumber").value("CLM-123"));
    }

    @Test
    @DisplayName("PUT /api/admin/claims/{claimId}/settle -> 200 OK")
    void settleClaim_ShouldReturn200() throws Exception {
        when(adminClaimsService.settleClaim(eq(1L), anyString(), anyString())).thenReturn(claimResponse);

        mockMvc.perform(put("/api/admin/claims/1/settle")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.claimNumber").value("CLM-123"));
    }
}
