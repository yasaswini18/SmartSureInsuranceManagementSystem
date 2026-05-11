package com.InsuranceManagementSystem.PolicyService.dto;

import com.InsuranceManagementSystem.PolicyService.entity.PolicyStatus;
import com.InsuranceManagementSystem.PolicyService.entity.PolicyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchasedPolicyResponse {

    private Long id;
    private String policyNumber;
    private String customerEmail;
    private Long productId;
    private String productName;
    private PolicyType policyType;
    private BigDecimal coverageAmount;
    private BigDecimal premiumPaid;
    private LocalDate startDate;
    private LocalDate endDate;
    private PolicyStatus status;
    private Long daysRemaining;
    private String extraDetailsJson;
    private LocalDateTime createdAt;
}
