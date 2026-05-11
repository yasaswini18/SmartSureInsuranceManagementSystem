package com.InsuranceManagementSystem.PolicyService.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyPurchasedMessage {
    private Long policyId;
    private String policyNumber;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String policyName;
    private String policyType;
    private Double basePremium;
    private Double coverageAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime purchasedAt;
}
