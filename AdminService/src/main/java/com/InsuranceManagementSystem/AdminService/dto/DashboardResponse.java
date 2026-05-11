package com.InsuranceManagementSystem.AdminService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private Long totalUsers;
    private Long totalAdmins;
    private Long totalCustomers;

    private Long totalPolicyProducts;
    private Long activePolicyProducts;
    private Long totalPurchasedPolicies;
    private Long activePolicies;
    private Long expiredPolicies;
    private Long cancelledPolicies;

    private Long totalClaims;
    private Long pendingClaims;
    private Long underReviewClaims;
    private Long approvedClaims;
    private Long rejectedClaims;
    private Long settledClaims;

    private BigDecimal totalPremiumCollected;
    private BigDecimal totalClaimedAmount;
    private BigDecimal totalApprovedAmount;
    private BigDecimal totalSettledAmount;

    private Long recentAuditActions;

    private LocalDateTime generatedAt;
}