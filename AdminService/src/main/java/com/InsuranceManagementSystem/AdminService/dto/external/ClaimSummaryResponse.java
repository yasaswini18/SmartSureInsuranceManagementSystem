package com.InsuranceManagementSystem.AdminService.dto.external;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimSummaryResponse {
    private Long id;
    private String claimNumber;
    private String policyNumber;
    private String productName;
    private String claimType;
    private BigDecimal claimedAmount;
    private BigDecimal approvedAmount;
    private String status;
    private LocalDateTime createdAt;
    private Long daysSinceFiled;
}