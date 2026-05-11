package com.InsuranceManagementSystem.AdminService.external;

import com.InsuranceManagementSystem.AdminService.config.OpenFeignConfig;
import com.InsuranceManagementSystem.AdminService.dto.external.ExternalApiResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "AuthService",
//        url = "${auth.service.url}",
        configuration = OpenFeignConfig.class
)
public interface AuthServiceClient {

    @GetMapping("/api/auth/users")
    ExternalApiResponse<List<UserResponse>> getAllUsers(
            @RequestHeader("Authorization") String bearerToken
    );

    @GetMapping("/api/auth/users/{email}")
    ExternalApiResponse<UserResponse> getUserByEmail(
            @PathVariable("email") String email,
            @RequestHeader("Authorization") String bearerToken
    );

    @GetMapping("/api/auth/users/role/{role}")
    ExternalApiResponse<List<UserResponse>> getUsersByRole(
            @PathVariable("role") String role,
            @RequestHeader("Authorization") String bearerToken
    );

    @GetMapping("/api/auth/users/count")
    ExternalApiResponse<Long> getTotalUserCount(
            @RequestHeader("Authorization") String bearerToken
    );

    @PostMapping("/api/auth/create-admin")
    ExternalApiResponse<Object> createAdmin(
            @RequestBody Object registerRequest,
            @RequestHeader("Authorization") String bearerToken
    );

    @GetMapping("/api/auth/health")
    String checkHealth();
}