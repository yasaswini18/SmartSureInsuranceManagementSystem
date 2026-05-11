package com.InsuranceManagementSystem.ClaimsService.dto;

import com.InsuranceManagementSystem.ClaimsService.enums.ClaimStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimReviewRequest {

    @NotNull(message = "Review decision is required")
    private ClaimStatus decision;

    @NotBlank(message = "Admin remarks are required")
    @Size(min = 10, max = 1000, message = "Remarks must be between 10 and 1000 characters")
    private String adminRemarks;

    private BigDecimal approvedAmount;
}