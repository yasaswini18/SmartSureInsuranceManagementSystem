package com.InsuranceManagementSystem.PolicyService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyValidationResponse {

    private boolean valid;
    private String policyNumber;
    private String customerEmail;
    private String productName;
    private String policyType;
    private BigDecimal coverageAmount;
    private LocalDate startDate;
    private String status;
    private String message;
}
