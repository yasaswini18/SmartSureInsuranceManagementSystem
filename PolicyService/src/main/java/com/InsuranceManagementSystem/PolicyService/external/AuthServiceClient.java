package com.InsuranceManagementSystem.PolicyService.external;

import com.InsuranceManagementSystem.PolicyService.config.OpenFeignConfig;
import com.InsuranceManagementSystem.PolicyService.dto.AuthValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "auth-service",
        url = "${auth.service.url}",
        configuration = OpenFeignConfig.class
)
public interface AuthServiceClient {

    @GetMapping("/api/auth/validate")
    AuthValidationResponse validateToken(
            @RequestHeader("Authorization") String bearerToken
    );

    @GetMapping("/api/auth/health")
    String checkHealth();
}