package com.InsuranceManagementSystem.AdminService.service;

import com.InsuranceManagementSystem.AdminService.dto.external.ExternalApiResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.ClaimResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.ClaimReviewRequest;
import com.InsuranceManagementSystem.AdminService.dto.external.ClaimSummaryResponse;
import com.InsuranceManagementSystem.AdminService.enums.AuditAction;
import com.InsuranceManagementSystem.AdminService.external.ClaimsServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminClaimsServiceTest {

    @Mock
    private ClaimsServiceClient claimsServiceClient;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AdminClaimsService adminClaimsService;

    private ClaimSummaryResponse summaryResponse;
    private ClaimResponse claimResponse;
    private final String adminEmail = "admin@insurance.com";
    private final String bearerToken = "Bearer token";

    @BeforeEach
    void setUp() {
        summaryResponse = new ClaimSummaryResponse();
        summaryResponse.setId(1L);
        summaryResponse.setClaimNumber("CLM-123");
        summaryResponse.setPolicyNumber("POL-123");
        summaryResponse.setStatus("PENDING");

        claimResponse = new ClaimResponse();
        claimResponse.setId(1L);
        claimResponse.setClaimNumber("CLM-123");
        claimResponse.setPolicyNumber("POL-123");
        claimResponse.setCustomerEmail("customer@test.com");
        claimResponse.setClaimedAmount(new BigDecimal("1000"));
        claimResponse.setStatus("PENDING");
    }

    private <T> ExternalApiResponse<T> createSuccessResponse(T data) {
        ExternalApiResponse<T> res = new ExternalApiResponse<>();
        res.setSuccess(true);
        res.setMessage("Success");
        res.setData(data);
        return res;
    }

    @Test
    @DisplayName("Should get all claims and log success")
    void getAllClaims_ShouldReturnListAndLog() {
        ExternalApiResponse<List<ClaimSummaryResponse>> response = createSuccessResponse(List.of(summaryResponse));
        when(claimsServiceClient.getAllClaims(bearerToken)).thenReturn(response);

        List<ClaimSummaryResponse> result = adminClaimsService.getAllClaims(adminEmail, bearerToken);

        assertThat(result).hasSize(1);
        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.VIEW_ALL_CLAIMS), eq("CLAIM"), eq("ALL"), anyString());
    }

    @Test
    @DisplayName("Should get pending claims and log success")
    void getPendingClaims_ShouldReturnListAndLog() {
        ExternalApiResponse<List<ClaimSummaryResponse>> response = createSuccessResponse(List.of(summaryResponse));
        when(claimsServiceClient.getPendingClaims(bearerToken)).thenReturn(response);

        List<ClaimSummaryResponse> result = adminClaimsService.getPendingClaims(adminEmail, bearerToken);

        assertThat(result).hasSize(1);
        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.VIEW_PENDING_CLAIMS), eq("CLAIM"), eq("PENDING"), anyString());
    }

    @Test
    @DisplayName("Should get claim by ID and log success")
    void getClaimById_ShouldReturnClaimAndLog() {
        ExternalApiResponse<ClaimResponse> response = createSuccessResponse(claimResponse);
        when(claimsServiceClient.getClaimById(1L, bearerToken)).thenReturn(response);

        ClaimResponse result = adminClaimsService.getClaimById(1L, adminEmail, bearerToken);

        assertThat(result).isNotNull();
        assertThat(result.getClaimNumber()).isEqualTo("CLM-123");
        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.VIEW_CLAIM), eq("CLAIM"), eq("CLM-123"), anyString());
    }

    @Test
    @DisplayName("Should start review successfully and log")
    void startReview_ShouldReturnUpdatedClaimAndLog() {
        ExternalApiResponse<ClaimResponse> response = createSuccessResponse(claimResponse);
        when(claimsServiceClient.startReview(1L, bearerToken)).thenReturn(response);

        ClaimResponse result = adminClaimsService.startReview(1L, adminEmail, bearerToken);

        assertThat(result).isNotNull();
        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.START_CLAIM_REVIEW), eq("CLAIM"), eq("CLM-123"), anyString());
    }

    @Test
    @DisplayName("Should log failure when start review throws exception")
    void startReview_WhenException_ShouldLogFailureAndThrow() {
        when(claimsServiceClient.startReview(1L, bearerToken)).thenThrow(new RuntimeException("Service unavailable"));

        assertThatThrownBy(() -> adminClaimsService.startReview(1L, adminEmail, bearerToken))
                .isInstanceOf(RuntimeException.class);

        verify(auditLogService).logFailure(eq(adminEmail), eq(AuditAction.START_CLAIM_REVIEW), eq("CLAIM"), eq("1"), anyString());
    }

    @Test
    @DisplayName("Should review claim (Approve) successfully and publish event")
    void reviewClaim_Approve_ShouldUpdateAndPublish() {
        ClaimReviewRequest request = new ClaimReviewRequest("APPROVED", "All good", new BigDecimal("1000"));
        ExternalApiResponse<ClaimResponse> response = createSuccessResponse(claimResponse);
        
        when(claimsServiceClient.reviewClaim(eq(1L), any(), eq(bearerToken))).thenReturn(response);

        ClaimResponse result = adminClaimsService.reviewClaim(1L, request, adminEmail, bearerToken);

        assertThat(result).isNotNull();
        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.APPROVE_CLAIM), eq("CLAIM"), eq("CLM-123"), anyString());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("Should review claim (Reject) successfully without exception")
    void reviewClaim_Reject_ShouldUpdate() {
        ClaimReviewRequest request = new ClaimReviewRequest("REJECTED", "Missing docs", null);
        ExternalApiResponse<ClaimResponse> response = createSuccessResponse(claimResponse);
        
        when(claimsServiceClient.reviewClaim(eq(1L), any(), eq(bearerToken))).thenReturn(response);

        ClaimResponse result = adminClaimsService.reviewClaim(1L, request, adminEmail, bearerToken);

        assertThat(result).isNotNull();
        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.REJECT_CLAIM), eq("CLAIM"), eq("CLM-123"), anyString());
    }

    @Test
    @DisplayName("Should log failure when review claim throws exception")
    void reviewClaim_WhenException_ShouldLogFailure() {
        ClaimReviewRequest request = new ClaimReviewRequest("APPROVED", "All good", new BigDecimal("1000"));
        when(claimsServiceClient.reviewClaim(eq(1L), any(), eq(bearerToken))).thenThrow(new RuntimeException("Error"));

        assertThatThrownBy(() -> adminClaimsService.reviewClaim(1L, request, adminEmail, bearerToken))
                .isInstanceOf(RuntimeException.class);

        verify(auditLogService).logFailure(eq(adminEmail), eq(AuditAction.APPROVE_CLAIM), eq("CLAIM"), eq("1"), anyString());
    }

    @Test
    @DisplayName("Should settle claim successfully and log")
    void settleClaim_ShouldUpdateAndLog() {
        ExternalApiResponse<ClaimResponse> response = createSuccessResponse(claimResponse);
        when(claimsServiceClient.settleClaim(1L, bearerToken)).thenReturn(response);

        ClaimResponse result = adminClaimsService.settleClaim(1L, adminEmail, bearerToken);

        assertThat(result).isNotNull();
        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.SETTLE_CLAIM), eq("CLAIM"), eq("CLM-123"), anyString());
    }

    @Test
    @DisplayName("Should log failure when settle claim throws exception")
    void settleClaim_WhenException_ShouldLogFailure() {
        when(claimsServiceClient.settleClaim(1L, bearerToken)).thenThrow(new RuntimeException("Error"));

        assertThatThrownBy(() -> adminClaimsService.settleClaim(1L, adminEmail, bearerToken))
                .isInstanceOf(RuntimeException.class);

        verify(auditLogService).logFailure(eq(adminEmail), eq(AuditAction.SETTLE_CLAIM), eq("CLAIM"), eq("1"), anyString());
    }
}
