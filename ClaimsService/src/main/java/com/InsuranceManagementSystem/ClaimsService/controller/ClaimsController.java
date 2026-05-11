package com.InsuranceManagementSystem.ClaimsService.controller;

import com.InsuranceManagementSystem.ClaimsService.dto.*;
import com.InsuranceManagementSystem.ClaimsService.service.ClaimsService;
import com.InsuranceManagementSystem.ClaimsService.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

/**
 * REST controller for managing claims endpoints.
 * Provides APIs for initiating, reviewing, tracking, and settling claims.
 */
@RestController
@Slf4j
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(
        name = "Claims",
        description = "Claim submission, document upload and tracking"
)
public class ClaimsController {

    private final ClaimsService claimsService;
    private final DocumentService documentService;

    /**
     * Initiates a new claim for a user.
     *
     * @param request     The request details for the claim.
     * @param bearerToken The user's authorization token.
     * @return ApiResponse containing the created ClaimResponse.
     */
    @PostMapping("/initiate")
    @PreAuthorize("hasRole('USER')")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "File a new claim",
            description = "Customer files claim against active policy. Requires USER token."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Claim filed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<ClaimResponse>> initiateClaim(
            @Valid @RequestBody InitiateClaimRequest request,
            @RequestHeader("Authorization") String bearerToken
    ) {
        String customerEmail = getCurrentUserEmail();

        ClaimResponse response = claimsService.initiateClaim(
                request,
                customerEmail,
                bearerToken
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Claim filed successfully. Claim Number: " +
                                response.getClaimNumber(),
                        response
                ));
    }

    /**
     * Uploads a supporting document for an existing claim.
     *
     * @param claimId The ID of the claim.
     * @param file    The document file to be uploaded.
     * @return ApiResponse containing the uploaded document details.
     * @throws IOException If file processing fails.
     */
    @PostMapping("/{claimId}/documents")
    @PreAuthorize("hasRole('USER')")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Upload claim document",
            description = "Upload supporting documents. Allowed: PDF, JPEG, PNG. Max 10MB each."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Document uploaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file format"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<ClaimDocumentResponse>> uploadDocument(
            @PathVariable Long claimId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        String customerEmail = getCurrentUserEmail();

        ClaimDocumentResponse response =
                documentService.uploadDocument(
                        claimId,
                        file,
                        customerEmail
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Document uploaded successfully",
                        response
                ));
    }

    /**
     * Fetches claims for the currently authenticated user.
     *
     * @return ApiResponse containing the list of claims.
     */
    @GetMapping("/my-claims")
    @PreAuthorize("hasRole('USER')")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get my claims",
            description = "Returns all claims filed by logged in customer."
    )
    public ResponseEntity<ApiResponse<List<ClaimSummaryResponse>>> getMyClaims() {

        String customerEmail = getCurrentUserEmail();

        List<ClaimSummaryResponse> claims =
                claimsService.getMyClaims(customerEmail);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Your claims fetched successfully",
                        claims
                )
        );
    }

    /**
     * Tracks the status of a specific claim.
     *
     * @param claimId The ID of the claim to track.
     * @return ApiResponse containing the detailed status.
     */
    @GetMapping("/status/{claimId}")
    @PreAuthorize("hasRole('USER')")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Track claim status",
            description = "Returns full claim details including status and admin remarks."
    )
    public ResponseEntity<ApiResponse<ClaimResponse>> trackClaimStatus(
            @PathVariable Long claimId
    ) {

        String customerEmail = getCurrentUserEmail();

        ClaimResponse response =
                claimsService.trackClaimStatus(claimId, customerEmail);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Claim status fetched successfully",
                        response
                )
        );
    }

    /**
     * Fetches details of a claim by its ID.
     *
     * @param claimId The ID of the claim.
     * @return ApiResponse containing claim details.
     */
    @GetMapping("/{claimId}")
    public ResponseEntity<ApiResponse<ClaimResponse>> getClaimById(
            @PathVariable Long claimId
    ) {

        String email = getCurrentUserEmail();
        String role = getCurrentUserRole();

        ClaimResponse response = claimsService.getClaimById(
                claimId,
                email,
                role
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Claim fetched successfully",
                        response
                )
        );
    }

    /**
     * Retrieves all claims. Intended for admins.
     *
     * @return ApiResponse containing all claims.
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ClaimSummaryResponse>>> getAllClaims() {

        List<ClaimSummaryResponse> claims = claimsService.getAllClaims();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "All claims fetched successfully",
                        claims
                )
        );
    }

    /**
     * Retrieves all pending claims. Intended for admins.
     *
     * @return ApiResponse containing pending claims.
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ClaimSummaryResponse>>> getPendingClaims() {

        List<ClaimSummaryResponse> claims =
                claimsService.getPendingClaims();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Pending claims fetched successfully",
                        claims
                )
        );
    }

    /**
     * Retrieves all documents for a specific claim.
     *
     * @param claimId The ID of the claim.
     * @return ApiResponse containing the documents.
     */
    @GetMapping("/{claimId}/documents")
    public ResponseEntity<ApiResponse<List<ClaimDocumentResponse>>> getDocumentsForClaim(
            @PathVariable Long claimId
    ) {

        String email = getCurrentUserEmail();
        String role = getCurrentUserRole();

        List<ClaimDocumentResponse> docs =
                documentService.getDocumentsForClaim(
                        claimId,
                        email,
                        role
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Documents fetched successfully",
                        docs
                )
        );
    }

    /**
     * Downloads a specific document.
     *
     * @param documentId The ID of the document to download.
     * @return ResponseEntity with the document content.
     * @throws MalformedURLException If the file URL is invalid.
     */
    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long documentId
    ) throws MalformedURLException {

        String email = getCurrentUserEmail();
        String role = getCurrentUserRole();

        Resource resource = documentService.downloadDocument(
                documentId,
                email,
                role
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                resource.getFilename() + "\""
                )
                .body(resource);
    }

    /**
     * Starts the review process for a claim.
     *
     * @param claimId The ID of the claim.
     * @return ApiResponse containing the updated claim.
     */
    @PutMapping("/{claimId}/start-review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClaimResponse>> startReview(
            @PathVariable Long claimId
    ) {
        String adminEmail = getCurrentUserEmail();

        ClaimResponse response =
                claimsService.startReview(claimId, adminEmail);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Claim review started. Status: UNDER_REVIEW",
                        response
                )
        );
    }

    /**
     * Completes the review of a claim, either approving or rejecting it.
     *
     * @param claimId The ID of the claim.
     * @param request The review details.
     * @return ApiResponse containing the reviewed claim.
     */
    @PutMapping("/{claimId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClaimResponse>> reviewClaim(
            @PathVariable Long claimId,
            @Valid @RequestBody ClaimReviewRequest request
    ) {
        String adminEmail = getCurrentUserEmail();

        ClaimResponse response = claimsService.reviewClaim(
                claimId,
                request,
                adminEmail
        );

        String message = request.getDecision().name().equals("APPROVED")
                ? "Claim approved successfully"
                : "Claim rejected successfully";

        return ResponseEntity.ok(
                ApiResponse.success(message, response)
        );
    }

    /**
     * Marks an approved claim as settled.
     *
     * @param claimId The ID of the claim.
     * @return ApiResponse containing the updated claim.
     */
    @PutMapping("/{claimId}/settle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClaimResponse>> settleClaim(
            @PathVariable Long claimId
    ) {
        String adminEmail = getCurrentUserEmail();

        ClaimResponse response =
                claimsService.settleClaim(claimId, adminEmail);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Claim settled successfully",
                        response
                )
        );
    }

    private String getCurrentUserEmail() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    private String getCurrentUserRole() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities()
                .stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("UNKNOWN");
    }
}