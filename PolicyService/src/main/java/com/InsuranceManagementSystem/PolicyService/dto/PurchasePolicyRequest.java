package com.InsuranceManagementSystem.PolicyService.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePolicyRequest {

    private Long productId;

    private Long policyTypeId;

    @NotNull(message = "Age is required")
    private Integer age;

    private Map<String, Object> extraDetails;
}
