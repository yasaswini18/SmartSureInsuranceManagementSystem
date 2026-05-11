package com.InsuranceManagementSystem.ClaimsService.external;

import com.InsuranceManagementSystem.ClaimsService.config.OpenFeignConfig;
import com.InsuranceManagementSystem.ClaimsService.dto.PolicyValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "PolicyService",
//        url = "${policy.service.url}",
        configuration = OpenFeignConfig.class
)
public interface PolicyServiceClient {

	@GetMapping("/api/policies/validate")
	PolicyValidationResponse validatePolicy(
	        @RequestParam("policyNumber") String policyNumber,
	        @RequestParam("customerEmail") String customerEmail,
	        @RequestHeader("Authorization") String bearerToken
	);

    @GetMapping("/api/policies/products")
    String checkHealth(
            @RequestHeader("Authorization") String bearerToken
    );
}