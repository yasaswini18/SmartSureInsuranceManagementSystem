package com.InsuranceManagementSystem.ClaimsService.dto;

import com.InsuranceManagementSystem.ClaimsService.enums.ClaimStatus;
import com.InsuranceManagementSystem.ClaimsService.enums.ClaimType;
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
public class ClaimSummaryResponse {

    private Long id;
    private String claimNumber;
    private String policyNumber;
    private String productName;
    private ClaimType claimType;
    private BigDecimal claimedAmount;
    private BigDecimal approvedAmount;
    private ClaimStatus status;
    private LocalDateTime createdAt;
    private Long daysSinceFiled;
}