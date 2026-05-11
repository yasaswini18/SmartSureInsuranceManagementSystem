package com.InsuranceManagementSystem.AdminService.controller;

import com.InsuranceManagementSystem.AdminService.dto.ApiResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.ClaimResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.ClaimReviewRequest;
import com.InsuranceManagementSystem.AdminService.dto.external.ClaimSummaryResponse;
import com.InsuranceManagementSystem.AdminService.service.AdminClaimsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for administrative management of insurance claims.
 * Provides endpoints for viewing, reviewing, approving, rejecting,
 * and settling claims. All endpoints require the ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/claims")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')") 
@io.swagger.v3.oas.annotations.tags.Tag(
        name = "Admin - Claims Management",
        description = "Admin claim review, approval and settlement. All endpoints require an ADMIN token."
)
public class AdminClaimsController {

    private final AdminClaimsService adminClaimsService;

    /**
     * Retrieves all claims in the system.
     *
     * @param bearerToken The administrator's authorization token.
     * @return {@link ResponseEntity} containing a list of {@link ClaimSummaryResponse}.
     */
    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get all claims",
            description = "Admin fetches all claims"
    )
    public ResponseEntity<ApiResponse<List<ClaimSummaryResponse>>> getAllClaims(
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = getCurrentUserEmail();

        List<ClaimSummaryResponse> claims =
                adminClaimsService.getAllClaims(adminEmail, bearerToken);

        return ResponseEntity.ok(
                ApiResponse.success("Claims fetched successfully", claims)
        );
    }

    /**
     * Retrieves all pending claims awaiting administrative review.
     *
     * @param bearerToken The administrator's authorization token.
     * @return {@link ResponseEntity} containing a list of {@link ClaimSummaryResponse}.
     */
    @GetMapping("/pending")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get pending claims",
            description = "Admin fetches all pending claims"
    )
    public ResponseEntity<ApiResponse<List<ClaimSummaryResponse>>> getPendingClaims(
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = getCurrentUserEmail();

        List<ClaimSummaryResponse> claims =
                adminClaimsService.getPendingClaims(adminEmail, bearerToken);

        return ResponseEntity.ok(
                ApiResponse.success("Pending claims fetched successfully", claims)
        );
    }

    /**
     * Retrieves detailed information for a specific claim by its ID.
     *
     * @param claimId     The unique identifier of the claim.
     * @param bearerToken The administrator's authorization token.
     * @return {@link ResponseEntity} containing the {@link ClaimResponse}.
     */
    @GetMapping("/{claimId}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get claim by ID",
            description = "Admin fetches detailed claim information"
    )
    public ResponseEntity<ApiResponse<ClaimResponse>> getClaimById(
            @PathVariable Long claimId,
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = getCurrentUserEmail();

        ClaimResponse claim =
                adminClaimsService.getClaimById(claimId, adminEmail, bearerToken);

        return ResponseEntity.ok(
                ApiResponse.success("Claim fetched successfully", claim)
        );
    }

    /**
     * Moves a pending claim into the UNDER_REVIEW status.
     *
     * @param claimId     The unique identifier of the claim.
     * @param bearerToken The administrator's authorization token.
     * @return {@link ResponseEntity} containing the updated {@link ClaimResponse}.
     */
    @PutMapping("/{claimId}/start-review")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Start claim review",
            description = "Admin moves claim from PENDING to UNDER_REVIEW status."
    )
    public ResponseEntity<ApiResponse<ClaimResponse>> startReview(
            @PathVariable Long claimId,
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = getCurrentUserEmail();

        ClaimResponse claim =
                adminClaimsService.startReview(claimId, adminEmail, bearerToken);

        return ResponseEntity.ok(
                ApiResponse.success("Claim review started. Status: UNDER_REVIEW", claim)
        );
    }

    /**
     * Submits an approval or rejection decision for a claim.
     *
     * @param claimId     The unique identifier of the claim.
     * @param request     The request body containing the decision, remarks, and amount.
     * @param bearerToken The administrator's authorization token.
     * @return {@link ResponseEntity} containing the updated {@link ClaimResponse}.
     */
    @PutMapping("/{claimId}/review")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Review claim",
            description = "Admin approves or rejects a claim with remarks and amount."
    )
    public ResponseEntity<ApiResponse<ClaimResponse>> reviewClaim(
            @PathVariable Long claimId,
            @Valid @RequestBody ClaimReviewRequest request,
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = getCurrentUserEmail();

        ClaimResponse claim =
                adminClaimsService.reviewClaim(claimId, request, adminEmail, bearerToken);

        String message = request.getDecision().equals("APPROVED")
                ? "Claim approved successfully"
                : "Claim rejected successfully";

        return ResponseEntity.ok(
                ApiResponse.success(message, claim)
        );
    }

    /**
     * Marks a previously approved claim as successfully settled.
     *
     * @param claimId     The unique identifier of the claim.
     * @param bearerToken The administrator's authorization token.
     * @return {@link ResponseEntity} containing the updated {@link ClaimResponse}.
     */
    @PutMapping("/{claimId}/settle")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Settle claim",
            description = "Admin marks approved claim as SETTLED after payment."
    )
    public ResponseEntity<ApiResponse<ClaimResponse>> settleClaim(
            @PathVariable Long claimId,
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = getCurrentUserEmail();

        ClaimResponse claim =
                adminClaimsService.settleClaim(claimId, adminEmail, bearerToken);

        return ResponseEntity.ok(
                ApiResponse.success("Claim settled successfully", claim)
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