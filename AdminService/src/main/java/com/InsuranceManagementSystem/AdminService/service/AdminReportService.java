package com.InsuranceManagementSystem.AdminService.service;

import com.InsuranceManagementSystem.AdminService.dto.DashboardResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.ClaimSummaryResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.PolicyProductResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.PurchasedPolicyResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.UserResponse;
import com.InsuranceManagementSystem.AdminService.enums.AuditAction;
import com.InsuranceManagementSystem.AdminService.external.AuthServiceClient;
import com.InsuranceManagementSystem.AdminService.external.ClaimsServiceClient;
import com.InsuranceManagementSystem.AdminService.external.PolicyServiceClient;
import com.InsuranceManagementSystem.AdminService.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for generating comprehensive administrative reports
 * and dashboard statistics by aggregating data from across all microservices.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminReportService {

    private final AuthServiceClient authServiceClient;
    private final PolicyServiceClient policyServiceClient;
    private final ClaimsServiceClient claimsServiceClient;
    private final AuditLogRepository auditLogRepository;
    private final AuditLogService auditLogService;

    /**
     * Generates a comprehensive dashboard report aggregating users,
     * policies, and claims statistics.
     *
     * @param adminEmail  The email of the administrator.
     * @param bearerToken The administrator's authorization token.
     * @return {@link DashboardResponse} containing aggregated metrics.
     */
    public DashboardResponse getDashboard(
            String adminEmail,
            String bearerToken
    ) {
        log.info("Admin {} generating dashboard", adminEmail);

        List<UserResponse> allUsers =
                authServiceClient.getAllUsers(bearerToken).getData();

        long totalUsers = allUsers.size();
        long totalAdmins = allUsers.stream()
                .filter(u -> u.getRole().equals("ADMIN"))
                .count();
        long totalCustomers = allUsers.stream()
                .filter(u -> u.getRole().equals("USER"))
                .count();

        List<PolicyProductResponse> allProducts =
                policyServiceClient.getAllProducts(bearerToken).getData();

        long totalProducts = allProducts.size();
        long activeProducts = allProducts.stream()
                .filter(PolicyProductResponse::getIsActive)
                .count();

        List<PurchasedPolicyResponse> allPolicies =
                policyServiceClient.getAllPurchasedPolicies(bearerToken).getData();

        long totalPolicies = allPolicies.size();
        long activePolicies = allPolicies.stream()
                .filter(p -> p.getStatus().equals("ACTIVE"))
                .count();
        long expiredPolicies = allPolicies.stream()
                .filter(p -> p.getStatus().equals("EXPIRED"))
                .count();
        long cancelledPolicies = allPolicies.stream()
                .filter(p -> p.getStatus().equals("CANCELLED"))
                .count();

        BigDecimal totalPremium = allPolicies.stream()
                .map(PurchasedPolicyResponse::getPremiumPaid)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ClaimSummaryResponse> allClaims =
                claimsServiceClient.getAllClaims(bearerToken).getData();

        long totalClaims = allClaims.size();
        long pendingClaims = allClaims.stream()
                .filter(c -> c.getStatus().equals("PENDING"))
                .count();
        long underReviewClaims = allClaims.stream()
                .filter(c -> c.getStatus().equals("UNDER_REVIEW"))
                .count();
        long approvedClaims = allClaims.stream()
                .filter(c -> c.getStatus().equals("APPROVED"))
                .count();
        long rejectedClaims = allClaims.stream()
                .filter(c -> c.getStatus().equals("REJECTED"))
                .count();
        long settledClaims = allClaims.stream()
                .filter(c -> c.getStatus().equals("SETTLED"))
                .count();

        BigDecimal totalClaimed = allClaims.stream()
                .map(ClaimSummaryResponse::getClaimedAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalApproved = allClaims.stream()
                .filter(c -> c.getApprovedAmount() != null)
                .map(ClaimSummaryResponse::getApprovedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSettled = allClaims.stream()
                .filter(c -> c.getStatus().equals("SETTLED")
                        && c.getApprovedAmount() != null)
                .map(ClaimSummaryResponse::getApprovedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long recentActions =
                auditLogRepository.findTop20ByOrderByPerformedAtDesc().size();

        DashboardResponse dashboard = DashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalAdmins(totalAdmins)
                .totalCustomers(totalCustomers)
                .totalPolicyProducts(totalProducts)
                .activePolicyProducts(activeProducts)
                .totalPurchasedPolicies(totalPolicies)
                .activePolicies(activePolicies)
                .expiredPolicies(expiredPolicies)
                .cancelledPolicies(cancelledPolicies)
                .totalClaims(totalClaims)
                .pendingClaims(pendingClaims)
                .underReviewClaims(underReviewClaims)
                .approvedClaims(approvedClaims)
                .rejectedClaims(rejectedClaims)
                .settledClaims(settledClaims)
                .totalPremiumCollected(totalPremium)
                .totalClaimedAmount(totalClaimed)
                .totalApprovedAmount(totalApproved)
                .totalSettledAmount(totalSettled)
                .recentAuditActions((long) recentActions)
                .generatedAt(LocalDateTime.now())
                .build();

        auditLogService.logSuccess(
                adminEmail,
                AuditAction.VIEW_DASHBOARD,
                "REPORT",
                "DASHBOARD",
                "Generated admin dashboard report"
        );

        log.info("Dashboard generated successfully for admin: {}", adminEmail);

        return dashboard;
    }

    /**
     * Retrieves a report of all claims in the system.
     *
     * @param adminEmail  The email of the administrator.
     * @param bearerToken The administrator's authorization token.
     * @return List of {@link ClaimSummaryResponse} representing the claims report.
     */
    public List<ClaimSummaryResponse> getClaimsReport(
            String adminEmail,
            String bearerToken
    ) {
        log.info("Admin {} generating claims report", adminEmail);

        List<ClaimSummaryResponse> claims =
                claimsServiceClient.getAllClaims(bearerToken).getData();

        auditLogService.logSuccess(
                adminEmail,
                AuditAction.VIEW_CLAIMS_REPORT,
                "REPORT",
                "CLAIMS",
                "Generated claims report. Total claims: " + claims.size()
        );

        return claims;
    }

    /**
     * Retrieves a report of all purchased policies in the system.
     *
     * @param adminEmail  The email of the administrator.
     * @param bearerToken The administrator's authorization token.
     * @return List of {@link PurchasedPolicyResponse} representing the policy report.
     */
    public List<PurchasedPolicyResponse> getPolicyReport(
            String adminEmail,
            String bearerToken
    ) {
        log.info("Admin {} generating policy report", adminEmail);

        List<PurchasedPolicyResponse> policies =
                policyServiceClient.getAllPurchasedPolicies(bearerToken).getData();

        auditLogService.logSuccess(
                adminEmail,
                AuditAction.VIEW_POLICY_REPORT,
                "REPORT",
                "POLICIES",
                "Generated policy report. Total policies: " + policies.size()
        );

        return policies;
    }

    /**
     * Generates a revenue report by aggregating premiums and claim payouts.
     *
     * @param adminEmail  The email of the administrator.
     * @param bearerToken The administrator's authorization token.
     * @return {@link DashboardResponse} detailing the financial revenue metrics.
     */
    public DashboardResponse getRevenueReport(
            String adminEmail,
            String bearerToken
    ) {
        log.info("Admin {} generating revenue report", adminEmail);

        DashboardResponse report = getDashboard(adminEmail, bearerToken);

        auditLogService.logSuccess(
                adminEmail,
                AuditAction.VIEW_REVENUE_REPORT,
                "REPORT",
                "REVENUE",
                "Generated revenue report. Total premium: " +
                report.getTotalPremiumCollected() +
                " Total settled: " + report.getTotalSettledAmount()
        );

        return report;
    }

    /**
     * Retrieves the comprehensive list of audit logs for the system.
     *
     * @param adminEmail The email of the administrator.
     * @return List of {@link com.InsuranceManagementSystem.AdminService.entity.AuditLog}.
     */
    public List<com.InsuranceManagementSystem.AdminService.entity.AuditLog> getAuditLogsReport(
            String adminEmail
    ) {
        log.info("Admin {} viewing audit logs", adminEmail);

        List<com.InsuranceManagementSystem.AdminService.entity.AuditLog> logs =
                auditLogService.getAllLogs();

        auditLogService.logSuccess(
                adminEmail,
                AuditAction.VIEW_AUDIT_LOGS,
                "REPORT",
                "AUDIT_LOGS",
                "Viewed audit logs. Total entries: " + logs.size()
        );

        return logs;
    }
}