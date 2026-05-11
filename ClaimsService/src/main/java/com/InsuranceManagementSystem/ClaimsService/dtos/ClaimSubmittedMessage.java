package com.InsuranceManagementSystem.ClaimsService.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimSubmittedMessage {
    private Long claimId;
    private String claimNumber;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Long policyId;
    private String policyNumber;
    private Double claimAmount;
    private String incidentType;
    private LocalDate incidentDate;
    private LocalDateTime submittedAt;
}
