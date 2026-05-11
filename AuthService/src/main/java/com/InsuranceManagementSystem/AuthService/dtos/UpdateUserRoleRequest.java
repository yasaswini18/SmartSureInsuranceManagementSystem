package com.InsuranceManagementSystem.AuthService.dtos;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserRoleRequest {

    @NotBlank
    private String role;
}