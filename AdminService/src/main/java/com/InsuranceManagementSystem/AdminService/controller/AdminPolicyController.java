package com.InsuranceManagementSystem.AdminService.controller;

import com.InsuranceManagementSystem.AdminService.dto.ApiResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.PolicyProductRequest;
import com.InsuranceManagementSystem.AdminService.dto.external.PolicyProductResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.PurchasedPolicyResponse;
import com.InsuranceManagementSystem.AdminService.service.AdminPolicyService;
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
 * REST controller for administrative management of insurance policy products.
 * Provides endpoints for creating, updating, deactivating, reactivating,
 * and fetching policy products and purchased policies.
 * All endpoints require the ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/policies")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@io.swagger.v3.oas.annotations.tags.Tag(
        name = "Admin - Policy Management",
        description = "Admin policy products management. All endpoints require an ADMIN token."
)
public class AdminPolicyController {

    private final AdminPolicyService adminPolicyService;

    /**
     * Creates a new policy product.
     *
     * @param request     The request body containing the new policy product details.
     * @param bearerToken The administrator's authorization token.
     * @return {@link ResponseEntity} containing the created {@link PolicyProductResponse}.
     */
    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Create policy product",
            description = "Admin creates a new policy product."
    )
    public ResponseEntity<ApiResponse<PolicyProductResponse>> createProduct(
            @Valid @RequestBody PolicyProductRequest request,
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = getCurrentUserEmail();

        PolicyProductResponse response =
                adminPolicyService.createProduct(request, adminEmail, bearerToken);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Policy product created successfully", response));
    }

    /**
     * Updates an existing policy product.
     *
     * @param productId   The unique identifier of the product.
     * @param request     The updated policy product details.
     * @param bearerToken The administrator's authorization token.
     * @return {@link ResponseEntity} containing the updated {@link PolicyProductResponse}.
     */
    @PutMapping("/{productId}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Update policy product",
            description = "Admin updates an existing policy product."
    )
    public ResponseEntity<ApiResponse<PolicyProductResponse>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody PolicyProductRequest request,
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = getCurrentUserEmail();

        PolicyProductResponse response =
                adminPolicyService.updateProduct(productId, request, adminEmail, bearerToken);

        return ResponseEntity.ok(
                ApiResponse.success("Policy product updated successfully", response)
        );
    }

    /**
     * Deactivates a policy product so it can no longer be purchased.
     *
     * @param productId   The unique identifier of the product.
     * @param bearerToken The administrator's authorization token.
     * @return {@link ResponseEntity} confirming the deactivation.
     */
    @DeleteMapping("/{productId}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Deactivate policy product",
            description = "Admin deactivates a policy product."
    )
    public ResponseEntity<ApiResponse<Void>> deactivateProduct(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = getCurrentUserEmail();

        adminPolicyService.deactivateProduct(productId, adminEmail, bearerToken);

        return ResponseEntity.ok(
                ApiResponse.success("Policy product deactivated successfully")
        );
    }

    /**
     * Reactivates a deactivated policy product.
     *
     * @param productId   The unique identifier of the product.
     * @param bearerToken The administrator's authorization token.
     * @return {@link ResponseEntity} confirming the reactivation.
     */
    @PutMapping("/{productId}/reactivate")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Reactivate policy product",
            description = "Admin reactivates a previously deactivated policy product."
    )
    public ResponseEntity<ApiResponse<Void>> reactivateProduct(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = getCurrentUserEmail();

        adminPolicyService.reactivateProduct(productId, adminEmail, bearerToken);

        return ResponseEntity.ok(
                ApiResponse.success("Policy product reactivated successfully")
        );
    }

    /**
     * Retrieves all policy products.
     *
     * @param bearerToken The administrator's authorization token.
     * @return {@link ResponseEntity} containing a list of {@link PolicyProductResponse}.
     */
    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get all policy products",
            description = "Admin fetches all policy products."
    )
    public ResponseEntity<ApiResponse<List<PolicyProductResponse>>> getAllProducts(
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = getCurrentUserEmail();

        List<PolicyProductResponse> products =
                adminPolicyService.getAllProducts(adminEmail, bearerToken);

        return ResponseEntity.ok(
                ApiResponse.success("Policy products fetched successfully", products)
        );
    }

    /**
     * Retrieves detailed information for a specific policy product by its ID.
     *
     * @param productId   The unique identifier of the product.
     * @param bearerToken The administrator's authorization token.
     * @return {@link ResponseEntity} containing the {@link PolicyProductResponse}.
     */
    @GetMapping("/{productId}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get policy product by ID",
            description = "Admin fetches a specific policy product by ID."
    )
    public ResponseEntity<ApiResponse<PolicyProductResponse>> getProductById(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = getCurrentUserEmail();

        PolicyProductResponse product =
                adminPolicyService.getProductById(productId, adminEmail, bearerToken);

        return ResponseEntity.ok(
                ApiResponse.success("Policy product fetched successfully", product)
        );
    }

    /**
     * Retrieves all purchased policies in the system.
     *
     * @param bearerToken The administrator's authorization token.
     * @return {@link ResponseEntity} containing a list of {@link PurchasedPolicyResponse}.
     */
    @GetMapping("/purchased")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get all purchased policies",
            description = "Admin fetches all policies purchased by customers."
    )
    public ResponseEntity<ApiResponse<List<PurchasedPolicyResponse>>> getAllPurchasedPolicies(
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = getCurrentUserEmail();

        List<PurchasedPolicyResponse> policies =
                adminPolicyService.getAllPurchasedPolicies(adminEmail, bearerToken);

        return ResponseEntity.ok(
                ApiResponse.success("Purchased policies fetched successfully", policies)
        );
    }

    /**
     * Helper method to retrieve the currently authenticated admin's email.
     *
     * @return The admin's email address.
     */
    private String getCurrentUserEmail() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}