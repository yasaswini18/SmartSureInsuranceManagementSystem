package com.InsuranceManagementSystem.ClaimsService.dto;

import com.InsuranceManagementSystem.ClaimsService.enums.ClaimType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitiateClaimRequest {

    @NotBlank(message = "Policy number is required")
    private String policyNumber;

    @NotNull(message = "Claim type is required")
    private ClaimType claimType;

    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 2000, message = "Description must be between 20 and 2000 characters")
    private String description;

    @NotNull(message = "Incident date is required")
    private LocalDateTime incidentDate;

    @NotNull(message = "Claimed amount is required")
    @DecimalMin(value = "1.0", message = "Claimed amount must be greater than 0")
    private BigDecimal claimedAmount;
}