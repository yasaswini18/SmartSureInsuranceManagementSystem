package com.InsuranceManagementSystem.AdminService.controller;

import com.InsuranceManagementSystem.AdminService.dto.ApiResponse;
import com.InsuranceManagementSystem.AdminService.dto.DashboardResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.ClaimSummaryResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.PurchasedPolicyResponse;
import com.InsuranceManagementSystem.AdminService.entity.AuditLog;
import com.InsuranceManagementSystem.AdminService.service.AdminReportService;
import com.InsuranceManagementSystem.AdminService.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for generating administrative reports and dashboards.
 * Provides endpoints for aggregated data across claims, policies, and users,
 * as well as fetching audit logs. All endpoints require the ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@io.swagger.v3.oas.annotations.tags.Tag(
        name = "Admin - Reports & Dashboard",
        description = "Aggregated reporting, dashboards, and audit logs. All endpoints require an ADMIN token."
)
public class AdminReportController {

    private final AdminReportService adminReportService;
    private final AuditLogService auditLogService;

    /**
     * Retrieves the comprehensive admin dashboard containing aggregated statistics.
     *
     * @param bearerToken The administrator's authorization token.
     * @return {@link ResponseEntity} containing the {@link DashboardResponse}.
     */
    @GetMapping("/dashboard")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get admin dashboard",
            description = "Admin fetches aggregated dashboard statistics."
    )
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = getCurrentUserEmail();

        DashboardResponse dashboard =
                adminReportService.getDashboard(adminEmail, bearerToken);

        return ResponseEntity.ok(
                ApiResponse.success("Dashboard generated successfully", dashboard)
        );
    }

    /**
     * Retrieves the claims report.
     *
     * @param bearerToken The administrator's authorization token.
     * @return {@link ResponseEntity} containing a list of {@link ClaimSummaryResponse}.
     */
    @GetMapping("/claims")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get claims report",
            description = "Admin fetches report of all claims."
    )
    public ResponseEntity<ApiResponse<List<ClaimSummaryResponse>>> getClaimsReport(
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = getCurrentUserEmail();

        List<ClaimSummaryResponse> claims =
                adminReportService.getClaimsReport(adminEmail, bearerToken);

        return ResponseEntity.ok(
                ApiResponse.success("Claims report generated successfully", claims)
        );
    }

    /**
     * Retrieves the policies report.
     *
     * @param bearerToken The administrator's authorization token.
     * @return {@link ResponseEntity} containing a list of {@link PurchasedPolicyResponse}.
     */
    @GetMapping("/policies")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get policy report",
            description = "Admin fetches report of all purchased policies."
    )
    public ResponseEntity<ApiResponse<List<PurchasedPolicyResponse>>> getPolicyReport(
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = getCurrentUserEmail();

        List<PurchasedPolicyResponse> policies =
                adminReportService.getPolicyReport(adminEmail, bearerToken);

        return ResponseEntity.ok(
                ApiResponse.success("Policy report generated successfully", policies)
        );
    }

    /**
     * Retrieves the revenue report.
     *
     * @param bearerToken The administrator's authorization token.
     * @return {@link ResponseEntity} containing the {@link DashboardResponse} used for revenue.
     */
    @GetMapping("/revenue")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get revenue report",
            description = "Admin fetches revenue report based on dashboard data."
    )
    public ResponseEntity<ApiResponse<DashboardResponse>> getRevenueReport(
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = getCurrentUserEmail();

        DashboardResponse report =
                adminReportService.getRevenueReport(adminEmail, bearerToken);

        return ResponseEntity.ok(
                ApiResponse.success("Revenue report generated successfully", report)
        );
    }

    /**
     * Retrieves all audit logs for the system.
     *
     * @return {@link ResponseEntity} containing a list of {@link AuditLog}.
     */
    @GetMapping("/audit-logs")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get all audit logs",
            description = "Admin fetches the full audit log history."
    )
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs() {
        String adminEmail = getCurrentUserEmail();

        List<AuditLog> logs =
                adminReportService.getAuditLogsReport(adminEmail);

        return ResponseEntity.ok(
                ApiResponse.success("Audit logs fetched successfully", logs)
        );
    }

    /**
     * Retrieves audit logs specifically performed by a requested admin.
     *
     * @param adminEmail The email of the admin to filter by.
     * @return {@link ResponseEntity} containing a list of {@link AuditLog}.
     */
    @GetMapping("/audit-logs/{adminEmail}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get audit logs by admin",
            description = "Admin fetches audit logs filtered by a specific admin's email."
    )
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogsByAdmin(
            @PathVariable String adminEmail
    ) {
        List<AuditLog> logs =
                auditLogService.getLogsByAdmin(adminEmail);

        return ResponseEntity.ok(
                ApiResponse.success("Audit logs for " + adminEmail + " fetched successfully", logs)
        );
    }

    /**
     * Retrieves the 20 most recent audit logs.
     *
     * @return {@link ResponseEntity} containing a list of {@link AuditLog}.
     */
    @GetMapping("/audit-logs/recent")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get recent audit activity",
            description = "Admin fetches the 20 most recent audit logs."
    )
    public ResponseEntity<ApiResponse<List<AuditLog>>> getRecentActivity() {
        List<AuditLog> logs = auditLogService.getRecentLogs();

        return ResponseEntity.ok(
                ApiResponse.success("Recent activity fetched successfully", logs)
        );
    }

    /**
     * Helper method to retrieve the currently authenticated admin's email.
     *
     * @return The admin's email address.
     */
    private String getCurrentUserEmail() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}