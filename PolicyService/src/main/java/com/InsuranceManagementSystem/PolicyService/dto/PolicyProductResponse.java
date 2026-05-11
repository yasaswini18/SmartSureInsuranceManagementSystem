package com.InsuranceManagementSystem.PolicyService.dto;

import com.InsuranceManagementSystem.PolicyService.entity.PolicyType;
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
public class PolicyProductResponse {

    private Long id;
    private String name;
    private PolicyType type;
    private String description;
    private BigDecimal basePremium;
    private BigDecimal coverageAmount;
    private Integer durationMonths;
    private Integer minAge;
    private Integer maxAge;
    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
