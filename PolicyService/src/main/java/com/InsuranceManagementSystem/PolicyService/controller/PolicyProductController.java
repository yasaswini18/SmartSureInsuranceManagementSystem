package com.InsuranceManagementSystem.PolicyService.controller;

import com.InsuranceManagementSystem.PolicyService.dto.ApiResponse;
import com.InsuranceManagementSystem.PolicyService.dto.PolicyProductRequest;
import com.InsuranceManagementSystem.PolicyService.dto.PolicyProductResponse;
import com.InsuranceManagementSystem.PolicyService.entity.PolicyType;
import com.InsuranceManagementSystem.PolicyService.service.PolicyProductService;

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
 * REST Controller for managing policy products.
 * Provides endpoints for admins to create, update, and manage products,
 * and public endpoints for viewing active products.
 */
@RestController
@RequestMapping("/api/policies/products")
@RequiredArgsConstructor
@Slf4j
@io.swagger.v3.oas.annotations.tags.Tag(
    name = "Policy Products",
    description = "Insurance policy product catalogue management"
)
public class PolicyProductController {

    private final PolicyProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Create policy product",
        description = "Admin creates new insurance product. Requires ADMIN token."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Product created successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied"
        )
    })
    public ResponseEntity<ApiResponse<PolicyProductResponse>> createProduct(
            @Valid @RequestBody PolicyProductRequest request
    ) {
        String adminEmail = getCurrentUserEmail();

        PolicyProductResponse response =
                productService.createProduct(request, adminEmail);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Policy product created successfully",
                        response
                ));
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Update policy product",
        description = "Admin updates existing product. Requires ADMIN token."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Product updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Product not found"
        )
    })
    public ResponseEntity<ApiResponse<PolicyProductResponse>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody PolicyProductRequest request
    ) {
        String adminEmail = getCurrentUserEmail();

        PolicyProductResponse response =
                productService.updateProduct(productId, request, adminEmail);

        return ResponseEntity.ok(
                ApiResponse.success("Policy product updated successfully", response)
        );
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Deactivate policy product",
        description = "Admin deactivates product from catalogue. Requires ADMIN token."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Product deactivated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied"
        )
    })
    public ResponseEntity<ApiResponse<Void>> deactivateProduct(
            @PathVariable Long productId
    ) {
        String adminEmail = getCurrentUserEmail();
        productService.deactivateProduct(productId, adminEmail);

        return ResponseEntity.ok(
                ApiResponse.success("Policy product deactivated successfully")
        );
    }
    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get all active products",
        description = "Public endpoint. Returns all active insurance products."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Products fetched successfully"
        )
    })
    public ResponseEntity<ApiResponse<List<PolicyProductResponse>>> getAllActiveProducts() {

        List<PolicyProductResponse> products =
                productService.getAllActiveProducts();

        return ResponseEntity.ok(
                ApiResponse.success("Active policy products fetched successfully", products)
        );
    }
    
    @GetMapping("/{productId}")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get product by ID",
        description = "Fetch a single insurance product by its ID"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Product fetched successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Product not found"
        )
    })
    public ResponseEntity<ApiResponse<PolicyProductResponse>> getProductById(
            @PathVariable Long productId
    ) {
        PolicyProductResponse response = productService.getProductById(productId);

        return ResponseEntity.ok(
                ApiResponse.success("Policy product fetched successfully", response)
        );
    }
    
    @GetMapping("/type/{type}")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get products by type",
        description = "Filter products by HEALTH, VEHICLE, LIFE or PROPERTY"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Products fetched successfully"
        )
    })
    public ResponseEntity<ApiResponse<List<PolicyProductResponse>>> getProductsByType(
            @PathVariable PolicyType type
    ) {
        List<PolicyProductResponse> products =
                productService.getProductsByType(type);

        return ResponseEntity.ok(
                ApiResponse.success(
                        type.name() + " policy products fetched successfully",
                        products
                )
        );
    }
    @PutMapping("/{productId}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Reactivate policy product",
        description = "Admin reactivates a deactivated product. Requires ADMIN token."
    )
    public ResponseEntity<ApiResponse<Void>> reactivateProduct(
            @PathVariable Long productId
    ) {
        String adminEmail = getCurrentUserEmail();
        productService.reactivateProduct(productId, adminEmail);

        return ResponseEntity.ok(
                ApiResponse.success("Policy product reactivated successfully")
        );
    }
    private String getCurrentUserEmail() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}