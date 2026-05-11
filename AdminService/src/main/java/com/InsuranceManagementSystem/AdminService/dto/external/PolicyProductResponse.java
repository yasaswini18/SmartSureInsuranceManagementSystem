package com.InsuranceManagementSystem.AdminService.dto.external;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyProductResponse {
    private Long id;
    private String name;
    private String type;
    private String description;
    private BigDecimal basePremium;
    private BigDecimal coverageAmount;
    private Integer durationMonths;
    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}