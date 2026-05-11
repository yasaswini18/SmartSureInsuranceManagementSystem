package com.InsuranceManagementSystem.AuthService.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String message;
    private String accessToken;
    
    @JsonIgnore
    private String refreshToken;
    
    private String tokenType;
    private Long accessTokenExpiresIn;
    
    @JsonIgnore
    private Long refreshTokenExpiresIn;
    
    private Long userId;
    private String email;
    private String role;
    private String name;
}
