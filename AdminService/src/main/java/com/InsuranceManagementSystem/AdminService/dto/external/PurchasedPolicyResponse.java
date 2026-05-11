package com.InsuranceManagementSystem.AdminService.dto.external;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchasedPolicyResponse {
    private Long id;
    private String policyNumber;
    private String customerEmail;
    private Long productId;
    private String productName;
    private String policyType;
    private BigDecimal coverageAmount;
    private BigDecimal premiumPaid;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Long daysRemaining;
    private LocalDateTime createdAt;
}