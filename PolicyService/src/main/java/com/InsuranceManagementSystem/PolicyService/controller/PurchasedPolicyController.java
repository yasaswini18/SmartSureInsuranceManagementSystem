package com.InsuranceManagementSystem.PolicyService.controller;

import com.InsuranceManagementSystem.PolicyService.dto.ApiResponse;
import com.InsuranceManagementSystem.PolicyService.dto.PolicyValidationResponse;
import com.InsuranceManagementSystem.PolicyService.dto.PurchasePolicyRequest;
import com.InsuranceManagementSystem.PolicyService.dto.PurchasedPolicyResponse;
import com.InsuranceManagementSystem.PolicyService.service.PurchasedPolicyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing purchased policies.
 * Provides endpoints for customers to purchase and manage their policies,
 * and for admins to view all purchased policies.
 */
@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Slf4j
@io.swagger.v3.oas.annotations.tags.Tag(
    name = "Purchased Policies",
    description = "Customer policy purchase and management"
)
public class PurchasedPolicyController {

    private final PurchasedPolicyService purchasedPolicyService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Policy Service is UP");
    }
    
    @PostMapping("/purchase")
    @PreAuthorize("hasRole('USER')")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Purchase a policy",
        description = "Customer purchases an insurance policy. Requires USER token."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Policy purchased successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied"
        )
    })
    public ResponseEntity<ApiResponse<PurchasedPolicyResponse>> purchasePolicy(
            @Valid @RequestBody PurchasePolicyRequest request
    ) {
        String customerEmail = getCurrentUserEmail();

        PurchasedPolicyResponse response =
                purchasedPolicyService.purchasePolicy(request, customerEmail);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Policy purchased successfully. Policy Number: "
                                + response.getPolicyNumber(),
                        response
                ));
    }

    @GetMapping("/my-policies")
    @PreAuthorize("hasRole('USER')")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get my policies",
        description = "Returns all policies purchased by logged in customer."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Policies fetched successfully"
        )
    })
    public ResponseEntity<ApiResponse<List<PurchasedPolicyResponse>>> getMyPolicies() {

        String customerEmail = getCurrentUserEmail();

        List<PurchasedPolicyResponse> policies =
                purchasedPolicyService.getMyPolicies(customerEmail);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Your policies fetched successfully",
                        policies
                )
        );
    }

    @GetMapping("/{policyId}")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get policy by ID",
        description = "Fetch a policy by ID (accessible by owner or admin)"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Policy fetched successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Policy not found"
        )
    })
    public ResponseEntity<ApiResponse<PurchasedPolicyResponse>> getPolicyById(
            @PathVariable Long policyId
    ) {
        String currentEmail = getCurrentUserEmail();
        String currentRole = getCurrentUserRole();

        PurchasedPolicyResponse policy =
                purchasedPolicyService.getPolicyById(policyId, currentEmail, currentRole);

        return ResponseEntity.ok(
                ApiResponse.success("Policy fetched successfully", policy)
        );
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get all purchased policies",
        description = "Admin can fetch all purchased policies"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "All policies fetched successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied"
        )
    })
    public ResponseEntity<ApiResponse<List<PurchasedPolicyResponse>>> getAllPolicies() {

        List<PurchasedPolicyResponse> policies =
                purchasedPolicyService.getAllPolicies();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "All purchased policies fetched successfully",
                        policies
                )
        );
    }

    @PutMapping("/{policyId}/cancel")
    @PreAuthorize("hasRole('USER')")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Cancel policy",
        description = "Customer cancels their active policy."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Policy cancelled successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied"
        )
    })
    public ResponseEntity<ApiResponse<PurchasedPolicyResponse>> cancelPolicy(
            @PathVariable Long policyId
    ) {
        String customerEmail = getCurrentUserEmail();

        PurchasedPolicyResponse response =
                purchasedPolicyService.cancelPolicy(policyId, customerEmail);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Policy cancelled successfully",
                        response
                )
        );
    }

    @GetMapping("/validate")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Validate policy",
        description = "Validates a policy using policy number and customer email"
    )
    public ResponseEntity<PolicyValidationResponse> validatePolicy(
            @RequestParam String policyNumber,
            @RequestParam String customerEmail
    ) {
        return ResponseEntity.ok(
                purchasedPolicyService.validatePolicy(policyNumber, customerEmail)
        );
    }

    private String getCurrentUserEmail() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    private String getCurrentUserRole() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .orElse("UNKNOWN");
    }
}