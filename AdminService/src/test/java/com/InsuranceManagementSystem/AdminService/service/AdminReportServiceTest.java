package com.InsuranceManagementSystem.AdminService.service;

import com.InsuranceManagementSystem.AdminService.dto.external.ExternalApiResponse;
import com.InsuranceManagementSystem.AdminService.dto.DashboardResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.ClaimSummaryResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.PolicyProductResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.PurchasedPolicyResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.UserResponse;
import com.InsuranceManagementSystem.AdminService.entity.AuditLog;
import com.InsuranceManagementSystem.AdminService.enums.AuditAction;
import com.InsuranceManagementSystem.AdminService.external.AuthServiceClient;
import com.InsuranceManagementSystem.AdminService.external.ClaimsServiceClient;
import com.InsuranceManagementSystem.AdminService.external.PolicyServiceClient;
import com.InsuranceManagementSystem.AdminService.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceTest {

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private PolicyServiceClient policyServiceClient;

    @Mock
    private ClaimsServiceClient claimsServiceClient;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AdminReportService adminReportService;

    private final String adminEmail = "admin@insurance.com";
    private final String bearerToken = "Bearer token";

    @BeforeEach
    void setUp() {
    }

    private <T> ExternalApiResponse<T> createSuccessResponse(T data) {
        ExternalApiResponse<T> res = new ExternalApiResponse<>();
        res.setSuccess(true);
        res.setMessage("Success");
        res.setData(data);
        return res;
    }

    @Test
    @DisplayName("Should generate dashboard and log success")
    void getDashboard_ShouldReturnDashboard() {
        // Mock Users
        UserResponse user1 = new UserResponse();
        user1.setId(1L);
        user1.setFullName("user1");
        user1.setEmail("user1@mail.com");
        user1.setRole("USER");

        UserResponse admin1 = new UserResponse();
        admin1.setId(2L);
        admin1.setFullName("admin1");
        admin1.setEmail("admin1@mail.com");
        admin1.setRole("ADMIN");
        
        when(authServiceClient.getAllUsers(bearerToken)).thenReturn(createSuccessResponse(List.of(user1, admin1)));

        // Mock Products
        PolicyProductResponse prod1 = new PolicyProductResponse();
        prod1.setId(1L);
        prod1.setIsActive(true);
        
        PolicyProductResponse prod2 = new PolicyProductResponse();
        prod2.setId(2L);
        prod2.setIsActive(false);
        
        when(policyServiceClient.getAllProducts(bearerToken)).thenReturn(createSuccessResponse(List.of(prod1, prod2)));

        // Mock Policies
        PurchasedPolicyResponse pol1 = new PurchasedPolicyResponse();
        pol1.setId(1L);
        pol1.setStatus("ACTIVE");
        pol1.setPremiumPaid(new BigDecimal("100"));
        
        PurchasedPolicyResponse pol2 = new PurchasedPolicyResponse();
        pol2.setId(2L);
        pol2.setStatus("EXPIRED");
        pol2.setPremiumPaid(new BigDecimal("200"));
        
        PurchasedPolicyResponse pol3 = new PurchasedPolicyResponse();
        pol3.setId(3L);
        pol3.setStatus("CANCELLED");
        pol3.setPremiumPaid(new BigDecimal("50"));
        
        when(policyServiceClient.getAllPurchasedPolicies(bearerToken)).thenReturn(createSuccessResponse(List.of(pol1, pol2, pol3)));

        // Mock Claims
        ClaimSummaryResponse clm1 = new ClaimSummaryResponse();
        clm1.setId(1L);
        clm1.setStatus("PENDING");
        clm1.setClaimedAmount(new BigDecimal("500"));
        
        ClaimSummaryResponse clm2 = new ClaimSummaryResponse();
        clm2.setId(2L);
        clm2.setStatus("UNDER_REVIEW");
        clm2.setClaimedAmount(new BigDecimal("500"));
        
        ClaimSummaryResponse clm3 = new ClaimSummaryResponse();
        clm3.setId(3L);
        clm3.setStatus("APPROVED");
        clm3.setClaimedAmount(new BigDecimal("500"));
        clm3.setApprovedAmount(new BigDecimal("400"));
        
        ClaimSummaryResponse clm4 = new ClaimSummaryResponse();
        clm4.setId(4L);
        clm4.setStatus("REJECTED");
        clm4.setClaimedAmount(new BigDecimal("500"));
        
        ClaimSummaryResponse clm5 = new ClaimSummaryResponse();
        clm5.setId(5L);
        clm5.setStatus("SETTLED");
        clm5.setClaimedAmount(new BigDecimal("500"));
        clm5.setApprovedAmount(new BigDecimal("400"));
        
        when(claimsServiceClient.getAllClaims(bearerToken)).thenReturn(createSuccessResponse(List.of(clm1, clm2, clm3, clm4, clm5)));

        // Mock Audit Repository
        when(auditLogRepository.findTop20ByOrderByPerformedAtDesc()).thenReturn(new ArrayList<>());

        DashboardResponse result = adminReportService.getDashboard(adminEmail, bearerToken);

        assertThat(result).isNotNull();
        assertThat(result.getTotalUsers()).isEqualTo(2);
        assertThat(result.getTotalAdmins()).isEqualTo(1);
        assertThat(result.getTotalCustomers()).isEqualTo(1);
        
        assertThat(result.getTotalPolicyProducts()).isEqualTo(2);
        assertThat(result.getActivePolicyProducts()).isEqualTo(1);
        
        assertThat(result.getTotalPurchasedPolicies()).isEqualTo(3);
        assertThat(result.getActivePolicies()).isEqualTo(1);
        assertThat(result.getExpiredPolicies()).isEqualTo(1);
        assertThat(result.getCancelledPolicies()).isEqualTo(1);
        assertThat(result.getTotalPremiumCollected()).isEqualByComparingTo("350");
        
        assertThat(result.getTotalClaims()).isEqualTo(5);
        assertThat(result.getPendingClaims()).isEqualTo(1);
        assertThat(result.getUnderReviewClaims()).isEqualTo(1);
        assertThat(result.getApprovedClaims()).isEqualTo(1);
        assertThat(result.getRejectedClaims()).isEqualTo(1);
        assertThat(result.getSettledClaims()).isEqualTo(1);
        assertThat(result.getTotalClaimedAmount()).isEqualByComparingTo("2500");
        assertThat(result.getTotalApprovedAmount()).isEqualByComparingTo("800");
        assertThat(result.getTotalSettledAmount()).isEqualByComparingTo("400");
        
        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.VIEW_DASHBOARD), eq("REPORT"), eq("DASHBOARD"), anyString());
    }

    @Test
    @DisplayName("Should fetch claims report and log success")
    void getClaimsReport_ShouldReturnList() {
        when(claimsServiceClient.getAllClaims(bearerToken)).thenReturn(createSuccessResponse(List.of()));

        List<ClaimSummaryResponse> result = adminReportService.getClaimsReport(adminEmail, bearerToken);

        assertThat(result).isNotNull();
        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.VIEW_CLAIMS_REPORT), eq("REPORT"), eq("CLAIMS"), anyString());
    }

    @Test
    @DisplayName("Should fetch policy report and log success")
    void getPolicyReport_ShouldReturnList() {
        when(policyServiceClient.getAllPurchasedPolicies(bearerToken)).thenReturn(createSuccessResponse(List.of()));

        List<PurchasedPolicyResponse> result = adminReportService.getPolicyReport(adminEmail, bearerToken);

        assertThat(result).isNotNull();
        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.VIEW_POLICY_REPORT), eq("REPORT"), eq("POLICIES"), anyString());
    }

    @Test
    @DisplayName("Should fetch revenue report and log success")
    void getRevenueReport_ShouldReturnDashboard() {
        when(authServiceClient.getAllUsers(bearerToken)).thenReturn(createSuccessResponse(List.of()));
        when(policyServiceClient.getAllProducts(bearerToken)).thenReturn(createSuccessResponse(List.of()));
        when(policyServiceClient.getAllPurchasedPolicies(bearerToken)).thenReturn(createSuccessResponse(List.of()));
        when(claimsServiceClient.getAllClaims(bearerToken)).thenReturn(createSuccessResponse(List.of()));
        when(auditLogRepository.findTop20ByOrderByPerformedAtDesc()).thenReturn(new ArrayList<>());

        DashboardResponse result = adminReportService.getRevenueReport(adminEmail, bearerToken);

        assertThat(result).isNotNull();
        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.VIEW_DASHBOARD), eq("REPORT"), eq("DASHBOARD"), anyString());
        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.VIEW_REVENUE_REPORT), eq("REPORT"), eq("REVENUE"), anyString());
    }

    @Test
    @DisplayName("Should fetch audit logs and log success")
    void getAuditLogsReport_ShouldReturnList() {
        when(auditLogService.getAllLogs()).thenReturn(List.of(new AuditLog()));

        List<AuditLog> result = adminReportService.getAuditLogsReport(adminEmail);

        assertThat(result).hasSize(1);
        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.VIEW_AUDIT_LOGS), eq("REPORT"), eq("AUDIT_LOGS"), anyString());
    }
}
