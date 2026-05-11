package com.InsuranceManagementSystem.AdminService.dto.external;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyProductRequest {
    private String name;
    private String type;
    private String description;
    private BigDecimal basePremium;
    private BigDecimal coverageAmount;
    private Integer durationMonths;
    private Integer minAge;  
    private Integer maxAge;
}