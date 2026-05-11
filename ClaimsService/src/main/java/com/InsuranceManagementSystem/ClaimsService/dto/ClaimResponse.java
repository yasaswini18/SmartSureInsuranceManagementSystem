package com.InsuranceManagementSystem.ClaimsService.dto;

import com.InsuranceManagementSystem.ClaimsService.enums.ClaimStatus;
import com.InsuranceManagementSystem.ClaimsService.enums.ClaimType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {

    private Long id;
    private String claimNumber;
    private String customerEmail;

    private String policyNumber;
    private String productName;
    private String policyType;
    private BigDecimal coverageAmount;

    private ClaimType claimType;
    private String description;
    private LocalDateTime incidentDate;

    private BigDecimal claimedAmount;
    private BigDecimal approvedAmount;

    private ClaimStatus status;

    private String adminRemarks;
    private String reviewedBy;
    private LocalDateTime reviewedAt;

    private List<ClaimDocumentResponse> documents;

    private Long daysSinceFiled;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}