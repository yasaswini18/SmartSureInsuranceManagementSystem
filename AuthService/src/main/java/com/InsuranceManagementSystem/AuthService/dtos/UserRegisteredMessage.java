package com.InsuranceManagementSystem.AuthService.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredMessage {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime registeredAt;
}
