package com.InsuranceManagementSystem.AdminService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimReviewedMessage {
    private Long claimId;
    private String claimNumber;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String policyNumber;
    private Double claimedAmount;
    private Double approvedAmount;
    private String decision;
    private String adminRemarks;
    private LocalDateTime reviewedAt;
}
