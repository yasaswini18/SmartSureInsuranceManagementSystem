package com.InsuranceManagementSystem.AdminService.controller;

import com.InsuranceManagementSystem.AdminService.dto.DashboardResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.ClaimSummaryResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.PurchasedPolicyResponse;
import com.InsuranceManagementSystem.AdminService.entity.AuditLog;
import com.InsuranceManagementSystem.AdminService.security.JwtAuthFilter;
import com.InsuranceManagementSystem.AdminService.security.JwtUtil;
import com.InsuranceManagementSystem.AdminService.service.AdminReportService;
import com.InsuranceManagementSystem.AdminService.service.AuditLogService;
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

@WebMvcTest(AdminReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminReportService adminReportService;

    @MockBean
    private AuditLogService auditLogService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private DashboardResponse dashboardResponse;
    private ClaimSummaryResponse claimSummaryResponse;
    private PurchasedPolicyResponse purchasedPolicyResponse;
    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin@insurance.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        dashboardResponse = DashboardResponse.builder()
                .totalUsers(10L)
                .build();

        claimSummaryResponse = new ClaimSummaryResponse();
        claimSummaryResponse.setClaimNumber("CLM-123");

        purchasedPolicyResponse = new PurchasedPolicyResponse();
        purchasedPolicyResponse.setPolicyNumber("POL-123");

        auditLog = new AuditLog();
        auditLog.setAdminEmail("admin@insurance.com");
    }

    @Test
    @DisplayName("GET /api/admin/reports/dashboard -> 200 OK")
    void getDashboard_ShouldReturn200() throws Exception {
        when(adminReportService.getDashboard(anyString(), anyString())).thenReturn(dashboardResponse);

        mockMvc.perform(get("/api/admin/reports/dashboard")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalUsers").value(10));
    }

    @Test
    @DisplayName("GET /api/admin/reports/claims -> 200 OK")
    void getClaimsReport_ShouldReturn200() throws Exception {
        when(adminReportService.getClaimsReport(anyString(), anyString())).thenReturn(List.of(claimSummaryResponse));

        mockMvc.perform(get("/api/admin/reports/claims")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].claimNumber").value("CLM-123"));
    }

    @Test
    @DisplayName("GET /api/admin/reports/policies -> 200 OK")
    void getPolicyReport_ShouldReturn200() throws Exception {
        when(adminReportService.getPolicyReport(anyString(), anyString())).thenReturn(List.of(purchasedPolicyResponse));

        mockMvc.perform(get("/api/admin/reports/policies")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].policyNumber").value("POL-123"));
    }

    @Test
    @DisplayName("GET /api/admin/reports/revenue -> 200 OK")
    void getRevenueReport_ShouldReturn200() throws Exception {
        when(adminReportService.getRevenueReport(anyString(), anyString())).thenReturn(dashboardResponse);

        mockMvc.perform(get("/api/admin/reports/revenue")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalUsers").value(10));
    }

    @Test
    @DisplayName("GET /api/admin/reports/audit-logs -> 200 OK")
    void getAuditLogs_ShouldReturn200() throws Exception {
        when(adminReportService.getAuditLogsReport(anyString())).thenReturn(List.of(auditLog));

        mockMvc.perform(get("/api/admin/reports/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].adminEmail").value("admin@insurance.com"));
    }

    @Test
    @DisplayName("GET /api/admin/reports/audit-logs/{adminEmail} -> 200 OK")
    void getAuditLogsByAdmin_ShouldReturn200() throws Exception {
        when(auditLogService.getLogsByAdmin(anyString())).thenReturn(List.of(auditLog));

        mockMvc.perform(get("/api/admin/reports/audit-logs/admin@insurance.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].adminEmail").value("admin@insurance.com"));
    }

    @Test
    @DisplayName("GET /api/admin/reports/audit-logs/recent -> 200 OK")
    void getRecentActivity_ShouldReturn200() throws Exception {
        when(auditLogService.getRecentLogs()).thenReturn(List.of(auditLog));

        mockMvc.perform(get("/api/admin/reports/audit-logs/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].adminEmail").value("admin@insurance.com"));
    }
}
