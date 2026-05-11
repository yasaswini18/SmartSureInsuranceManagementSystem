package com.InsuranceManagementSystem.PolicyService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthValidationResponse {

    private String email;
    private String role;
    private boolean valid;
}