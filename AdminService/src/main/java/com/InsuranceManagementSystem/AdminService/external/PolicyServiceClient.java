package com.InsuranceManagementSystem.AdminService.external;

import com.InsuranceManagementSystem.AdminService.config.OpenFeignConfig;
import com.InsuranceManagementSystem.AdminService.dto.external.ExternalApiResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.PolicyProductRequest;
import com.InsuranceManagementSystem.AdminService.dto.external.PolicyProductResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.PurchasedPolicyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(
        name = "PolicyService",
        configuration = OpenFeignConfig.class
)
public interface PolicyServiceClient {

    @PostMapping("/api/policies/products")
    ExternalApiResponse<PolicyProductResponse> createProduct(
            @RequestBody PolicyProductRequest request,
            @RequestHeader("Authorization") String bearerToken
    );

    @PutMapping("/api/policies/products/{productId}")
    ExternalApiResponse<PolicyProductResponse> updateProduct(
            @PathVariable("productId") Long productId,
            @RequestBody PolicyProductRequest request,
            @RequestHeader("Authorization") String bearerToken
    );

    @DeleteMapping("/api/policies/products/{productId}")
    ExternalApiResponse<Void> deactivateProduct(
            @PathVariable("productId") Long productId,
            @RequestHeader("Authorization") String bearerToken
    );

    @PutMapping("/api/policies/products/{productId}/reactivate")
    ExternalApiResponse<Void> reactivateProduct(
            @PathVariable("productId") Long productId,
            @RequestHeader("Authorization") String bearerToken
    );

    @GetMapping("/api/policies/products")
    ExternalApiResponse<List<PolicyProductResponse>> getAllProducts(
            @RequestHeader("Authorization") String bearerToken
    );

    @GetMapping("/api/policies/products/{productId}")
    ExternalApiResponse<PolicyProductResponse> getProductById(
            @PathVariable("productId") Long productId,
            @RequestHeader("Authorization") String bearerToken
    );

    @GetMapping("/api/policies/all")
    ExternalApiResponse<List<PurchasedPolicyResponse>> getAllPurchasedPolicies(
            @RequestHeader("Authorization") String bearerToken
    );
}