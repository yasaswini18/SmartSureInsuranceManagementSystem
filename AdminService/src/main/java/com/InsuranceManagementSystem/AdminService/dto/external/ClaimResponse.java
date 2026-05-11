package com.InsuranceManagementSystem.AdminService.dto.external;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
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
    private String claimType;
    private String description;
    private LocalDateTime incidentDate;
    private BigDecimal claimedAmount;
    private BigDecimal approvedAmount;
    private String status;
    private String adminRemarks;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private List<Object> documents;
    private Long daysSinceFiled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}