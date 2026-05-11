package com.InsuranceManagementSystem.ClaimsService.service;

import com.InsuranceManagementSystem.ClaimsService.dto.*;
import com.InsuranceManagementSystem.ClaimsService.entity.Claim;
import com.InsuranceManagementSystem.ClaimsService.enums.ClaimStatus;
import com.InsuranceManagementSystem.ClaimsService.enums.ClaimType;
import com.InsuranceManagementSystem.ClaimsService.external.PolicyServiceClient;
import com.InsuranceManagementSystem.ClaimsService.repository.ClaimDocumentRepository;
import com.InsuranceManagementSystem.ClaimsService.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing claims lifecycle including initiation,
 * review, tracking, and settlement.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimsService {

    private final ClaimRepository claimRepository;
    private final ClaimDocumentRepository documentRepository;
    private final PolicyServiceClient policyServiceClient;
    private final DocumentService documentService;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @Value("${claim.number.prefix}")
    private String claimNumberPrefix;

    /**
     * Initiates a new claim for a given policy.
     *
     * @param request       The request containing claim details.
     * @param customerEmail The email of the customer filing the claim.
     * @param bearerToken   The authorization token of the customer.
     * @return ClaimResponse containing the created claim details.
     */
    @Transactional
    public ClaimResponse initiateClaim(
            InitiateClaimRequest request,
            String customerEmail,
            String bearerToken
    ) {

        PolicyValidationResponse policyValidation =
                policyServiceClient.validatePolicy(
                        request.getPolicyNumber(),
                        customerEmail,
                        bearerToken
                );

        if (!policyValidation.isValid()) {
            throw new RuntimeException(policyValidation.getMessage());
        }

        if (policyValidation.getStartDate() != null &&
            request.getIncidentDate().toLocalDate().isBefore(policyValidation.getStartDate().plusDays(7))) {
            throw new RuntimeException("Claims can only be filed for incidents occurring after 7 days from policy start date");
        }

        if (request.getIncidentDate().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Incident date cannot be in the future");
        }

        if (request.getIncidentDate().isBefore(LocalDateTime.now().minusYears(1))) {
            throw new RuntimeException("Incident date cannot be more than 1 year in the past");
        }

        if (request.getClaimedAmount().compareTo(policyValidation.getCoverageAmount()) > 0) {
            throw new RuntimeException("Claimed amount cannot exceed policy coverage amount");
        }

        boolean hasActiveClaim =
                claimRepository.existsByPolicyNumberAndStatusNotIn(
                        request.getPolicyNumber(),
                        List.of(ClaimStatus.REJECTED, ClaimStatus.SETTLED)
                );

        if (hasActiveClaim) {
            throw new RuntimeException("Active claim already exists for this policy");
        }

        validateClaimTypeVsPolicyType(
                request.getClaimType(),
                policyValidation.getPolicyType()
        );

        String claimNumber = generateClaimNumber();

        Claim claim = Claim.builder()
                .claimNumber(claimNumber)
                .policyNumber(request.getPolicyNumber())
                .customerEmail(customerEmail)
                .claimType(request.getClaimType())
                .description(request.getDescription())
                .incidentDate(request.getIncidentDate())
                .claimedAmount(request.getClaimedAmount())
                .status(ClaimStatus.PENDING)
                .productName(policyValidation.getProductName())
                .policyType(policyValidation.getPolicyType())
                .coverageAmount(policyValidation.getCoverageAmount())
                .build();

        Claim saved = claimRepository.save(claim);

        try {
            com.InsuranceManagementSystem.ClaimsService.dtos.ClaimSubmittedMessage message = new com.InsuranceManagementSystem.ClaimsService.dtos.ClaimSubmittedMessage(
                    saved.getId(),
                    saved.getClaimNumber(),
                    0L,
                    customerEmail,
                    customerEmail,
                    0L,
                    saved.getPolicyNumber(),
                    saved.getClaimedAmount().doubleValue(),
                    saved.getClaimType().name(),
                    saved.getIncidentDate().toLocalDate(),
                    saved.getCreatedAt()
            );
            rabbitTemplate.convertAndSend(
                    com.InsuranceManagementSystem.ClaimsService.config.RabbitMQConfig.EXCHANGE, 
                    com.InsuranceManagementSystem.ClaimsService.config.RabbitMQConfig.CLAIM_SUBMITTED_QUEUE, 
                    message
            );
            log.info("Published ClaimSubmittedMessage for claim: {}", saved.getClaimNumber());
        } catch(Exception e) {
            log.error("Failed to publish ClaimSubmittedMessage for claim: {}", saved.getClaimNumber(), e);
        }

        return mapToClaimResponse(saved);
    }

    /**
     * Retrieves a summary of all claims filed by a specific customer.
     *
     * @param customerEmail The email of the customer.
     * @return List of ClaimSummaryResponse.
     */
    public List<ClaimSummaryResponse> getMyClaims(String customerEmail) {
        return claimRepository
                .findByCustomerEmail(customerEmail)
                .stream()
                .map(this::mapToClaimSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tracks the current status of a specific claim.
     *
     * @param claimId       The ID of the claim.
     * @param customerEmail The email of the customer.
     * @return ClaimResponse containing detailed status.
     */
    public ClaimResponse trackClaimStatus(Long claimId, String customerEmail) {
        Claim claim = claimRepository
                .findByIdAndCustomerEmail(claimId, customerEmail)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        return mapToClaimResponse(claim);
    }

    /**
     * Retrieves all claims in the system. Intended for ADMIN use.
     *
     * @return List of all claims as ClaimSummaryResponse.
     */
    public List<ClaimSummaryResponse> getAllClaims() {
        return claimRepository.findAll()
                .stream()
                .map(this::mapToClaimSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all pending claims. Intended for ADMIN use.
     *
     * @return List of pending claims as ClaimSummaryResponse.
     */
    public List<ClaimSummaryResponse> getPendingClaims() {
        return claimRepository
                .findByStatus(ClaimStatus.PENDING)
                .stream()
                .map(this::mapToClaimSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves detailed information for a specific claim.
     *
     * @param claimId      The ID of the claim.
     * @param currentEmail The email of the user making the request.
     * @param currentRole  The role of the user.
     * @return ClaimResponse containing detailed claim information.
     */
    public ClaimResponse getClaimById(Long claimId, String currentEmail, String currentRole) {

        Claim claim;

        if (currentRole.equals("ADMIN")) {
            claim = claimRepository.findById(claimId)
                    .orElseThrow(() -> new RuntimeException("Claim not found"));
        } else {
            claim = claimRepository
                    .findByIdAndCustomerEmail(claimId, currentEmail)
                    .orElseThrow(() -> new RuntimeException("Unauthorized"));
        }

        return mapToClaimResponse(claim);
    }

    /**
     * Reviews a claim, either approving or rejecting it.
     *
     * @param claimId    The ID of the claim to review.
     * @param request    The review details.
     * @param adminEmail The email of the reviewing admin.
     * @return ClaimResponse containing the updated claim.
     */
    @Transactional
    public ClaimResponse reviewClaim(Long claimId, ClaimReviewRequest request, String adminEmail) {

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.PENDING &&
            claim.getStatus() != ClaimStatus.UNDER_REVIEW) {
            throw new RuntimeException("Invalid claim state");
        }

        if (request.getDecision() != ClaimStatus.APPROVED &&
            request.getDecision() != ClaimStatus.REJECTED) {
            throw new RuntimeException("Invalid decision");
        }

        if (request.getDecision() == ClaimStatus.APPROVED) {

            if (request.getApprovedAmount() == null ||
                request.getApprovedAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Invalid approved amount");
            }

            if (request.getApprovedAmount()
                    .compareTo(claim.getCoverageAmount()) > 0) {
                throw new RuntimeException("Exceeds coverage amount");
            }
        }

        claim.setStatus(request.getDecision());
        claim.setAdminRemarks(request.getAdminRemarks());
        claim.setReviewedBy(adminEmail);
        claim.setReviewedAt(LocalDateTime.now());

        if (request.getDecision() == ClaimStatus.APPROVED) {
            claim.setApprovedAmount(request.getApprovedAmount());
        }

        return mapToClaimResponse(claimRepository.save(claim));
    }
    
    /**
     * Moves a pending claim to UNDER_REVIEW status.
     *
     * @param claimId    The ID of the claim.
     * @param adminEmail The email of the admin starting the review.
     * @return ClaimResponse containing the updated claim.
     */
    @Transactional
    public ClaimResponse startReview(Long claimId, String adminEmail) {

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException(
                        "Claim not found with id: " + claimId
                ));

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new RuntimeException(
                    "Only PENDING claims can be moved to UNDER_REVIEW. Current status: "
                            + claim.getStatus().name()
            );
        }

        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        claim.setReviewedBy(adminEmail);

        Claim updated = claimRepository.save(claim);

        return mapToClaimResponse(updated);
    }
    
    /**
     * Marks an approved claim as SETTLED.
     *
     * @param claimId    The ID of the claim.
     * @param adminEmail The email of the admin settling the claim.
     * @return ClaimResponse containing the updated claim.
     */
    @Transactional
    public ClaimResponse settleClaim(Long claimId, String adminEmail) {

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException(
                        "Claim not found with id: " + claimId
                ));

        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new RuntimeException(
                    "Only APPROVED claims can be settled. Current status: "
                            + claim.getStatus().name()
            );
        }

        claim.setStatus(ClaimStatus.SETTLED);

        Claim updated = claimRepository.save(claim);

        return mapToClaimResponse(updated);
    }
    private void validateClaimTypeVsPolicyType(ClaimType claimType, String policyType) {

        if (claimType == ClaimType.MEDICAL && !policyType.equals("HEALTH")) {
            throw new RuntimeException("Invalid claim type for policy");
        }

        if ((claimType == ClaimType.THEFT ||
             claimType == ClaimType.ACCIDENTAL_DAMAGE) &&
            policyType.equals("HEALTH")) {
            throw new RuntimeException("Invalid claim type for HEALTH policy");
        }
    }

    private String generateClaimNumber() {
        int year = LocalDate.now().getYear();
        long totalClaims = claimRepository.count();
        return String.format("%s-%d-%06d",
                claimNumberPrefix, year, totalClaims + 1);
    }

    /**
     * Maps a Claim entity to a ClaimResponse DTO.
     *
     * @param claim The Claim entity.
     * @return ClaimResponse DTO.
     */
    public ClaimResponse mapToClaimResponse(Claim claim) {

        long daysSinceFiled = ChronoUnit.DAYS.between(
                claim.getCreatedAt().toLocalDate(),
                LocalDate.now()
        );

        List<ClaimDocumentResponse> documentResponses =
                claim.getDocuments()
                        .stream()
                        .map(doc -> ClaimDocumentResponse.builder()
                                .id(doc.getId())
                                .fileName(doc.getFileName())
                                .fileType(doc.getFileType())
                                .fileSize(formatFileSize(doc.getFileSize()))
                                .downloadUrl("/api/claims/documents/" + doc.getId() + "/download")
                                .uploadedAt(doc.getUploadedAt())
                                .build()
                        )
                        .collect(Collectors.toList());

        return ClaimResponse.builder()
                .id(claim.getId())
                .claimNumber(claim.getClaimNumber())
                .customerEmail(claim.getCustomerEmail())
                .policyNumber(claim.getPolicyNumber())
                .productName(claim.getProductName())
                .policyType(claim.getPolicyType())
                .coverageAmount(claim.getCoverageAmount())
                .claimType(claim.getClaimType())
                .description(claim.getDescription())
                .incidentDate(claim.getIncidentDate())
                .claimedAmount(claim.getClaimedAmount())
                .approvedAmount(claim.getApprovedAmount())
                .status(claim.getStatus())
                .adminRemarks(claim.getAdminRemarks())
                .reviewedBy(claim.getReviewedBy())
                .reviewedAt(claim.getReviewedAt())
                .documents(documentResponses)
                .daysSinceFiled(daysSinceFiled)
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }

    private ClaimSummaryResponse mapToClaimSummaryResponse(Claim claim) {

        long daysSinceFiled = ChronoUnit.DAYS.between(
                claim.getCreatedAt().toLocalDate(),
                LocalDate.now()
        );

        return ClaimSummaryResponse.builder()
                .id(claim.getId())
                .claimNumber(claim.getClaimNumber())
                .policyNumber(claim.getPolicyNumber())
                .productName(claim.getProductName())
                .claimType(claim.getClaimType())
                .claimedAmount(claim.getClaimedAmount())
                .approvedAmount(claim.getApprovedAmount())
                .status(claim.getStatus())
                .createdAt(claim.getCreatedAt())
                .daysSinceFiled(daysSinceFiled)
                .build();
    }

    private String formatFileSize(Long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}