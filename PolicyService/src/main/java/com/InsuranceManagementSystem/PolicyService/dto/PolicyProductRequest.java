package com.InsuranceManagementSystem.PolicyService.dto;

import com.InsuranceManagementSystem.PolicyService.entity.PolicyType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    @NotNull(message = "Policy type is required")
    private PolicyType type;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    @NotNull(message = "Base premium is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base premium must be greater than 0")
    private BigDecimal basePremium;

    @NotNull(message = "Coverage amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Coverage amount must be greater than 0")
    private BigDecimal coverageAmount;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 month")
    @Max(value = 360, message = "Duration cannot exceed 360 months")
    private Integer durationMonths;

    @NotNull(message = "Minimum age is required")
    @Min(value = 0, message = "Minimum age cannot be negative")
    @Max(value = 120, message = "Minimum age cannot exceed 120")
    private Integer minAge;

    @NotNull(message = "Maximum age is required")
    @Min(value = 1, message = "Maximum age must be at least 1")
    @Max(value = 120, message = "Maximum age cannot exceed 120")
    private Integer maxAge;
}
