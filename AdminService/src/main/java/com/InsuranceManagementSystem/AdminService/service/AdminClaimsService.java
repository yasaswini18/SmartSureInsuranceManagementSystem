package com.InsuranceManagementSystem.AdminService.service;

import com.InsuranceManagementSystem.AdminService.dto.external.ClaimResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.ClaimReviewRequest;
import com.InsuranceManagementSystem.AdminService.dto.external.ClaimSummaryResponse;
import com.InsuranceManagementSystem.AdminService.enums.AuditAction;
import com.InsuranceManagementSystem.AdminService.external.ClaimsServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing claims from the administrative perspective.
 * Orchestrates communication with the ClaimsService and records audit logs
 * for all administrative actions performed on claims.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminClaimsService {

    private final ClaimsServiceClient claimsServiceClient;
    private final AuditLogService auditLogService;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    /**
     * Retrieves all claims in the system.
     *
     * @param adminEmail  The email of the administrator.
     * @param bearerToken The administrator's authorization token.
     * @return List of {@link ClaimSummaryResponse} representing all claims.
     */
    public List<ClaimSummaryResponse> getAllClaims(
            String adminEmail,
            String bearerToken
    ) {
        log.info("Admin {} fetching all claims", adminEmail);

        List<ClaimSummaryResponse> claims =
                claimsServiceClient.getAllClaims(bearerToken).getData();

        auditLogService.logSuccess(
                adminEmail,
                AuditAction.VIEW_ALL_CLAIMS,
                "CLAIM",
                "ALL",
                "Viewed all claims. Count: " + claims.size()
        );

        return claims;
    }

    /**
     * Retrieves all pending claims awaiting review.
     *
     * @param adminEmail  The email of the administrator.
     * @param bearerToken The administrator's authorization token.
     * @return List of {@link ClaimSummaryResponse} representing pending claims.
     */
    public List<ClaimSummaryResponse> getPendingClaims(
            String adminEmail,
            String bearerToken
    ) {
        log.info("Admin {} fetching pending claims", adminEmail);

        List<ClaimSummaryResponse> claims =
                claimsServiceClient.getPendingClaims(bearerToken).getData();

        auditLogService.logSuccess(
                adminEmail,
                AuditAction.VIEW_PENDING_CLAIMS,
                "CLAIM",
                "PENDING",
                "Viewed pending claims. Count: " + claims.size()
        );

        return claims;
    }

    /**
     * Retrieves detailed information for a specific claim by its ID.
     *
     * @param claimId     The unique identifier of the claim.
     * @param adminEmail  The email of the administrator.
     * @param bearerToken The administrator's authorization token.
     * @return {@link ClaimResponse} containing claim details.
     */
    public ClaimResponse getClaimById(
            Long claimId,
            String adminEmail,
            String bearerToken
    ) {
        log.info("Admin {} fetching claim id: {}", adminEmail, claimId);

        ClaimResponse claim =
                claimsServiceClient.getClaimById(claimId, bearerToken).getData();

        auditLogService.logSuccess(
                adminEmail,
                AuditAction.VIEW_CLAIM,
                "CLAIM",
                claim.getClaimNumber(),
                "Viewed claim: " + claim.getClaimNumber()
        );

        return claim;
    }

    /**
     * Initiates the review process for a specific claim.
     *
     * @param claimId     The unique identifier of the claim.
     * @param adminEmail  The email of the administrator.
     * @param bearerToken The administrator's authorization token.
     * @return {@link ClaimResponse} containing the updated claim details.
     */
    public ClaimResponse startReview(
            Long claimId,
            String adminEmail,
            String bearerToken
    ) {
        log.info("Admin {} starting review of claim id: {}", adminEmail, claimId);

        try {
            ClaimResponse claim =
                    claimsServiceClient.startReview(claimId, bearerToken).getData();

            auditLogService.logSuccess(
                adminEmail,
                AuditAction.START_CLAIM_REVIEW,
                "CLAIM",
                claim.getClaimNumber(),
                "Started review of claim: " + claim.getClaimNumber()
            );

            return claim;

        } catch (Exception e) {
            auditLogService.logFailure(
                    adminEmail,
                    AuditAction.START_CLAIM_REVIEW,
                    "CLAIM",
                    claimId.toString(),
                    e.getMessage()
            );
            throw e;
        }
    }

    /**
     * Submits a review decision (Approval or Rejection) for a specific claim.
     * Also publishes a ClaimReviewedMessage to RabbitMQ upon successful review.
     *
     * @param claimId     The unique identifier of the claim.
     * @param request     The {@link ClaimReviewRequest} containing the decision.
     * @param adminEmail  The email of the administrator.
     * @param bearerToken The administrator's authorization token.
     * @return {@link ClaimResponse} containing the updated claim details.
     */
    public ClaimResponse reviewClaim(
            Long claimId,
            ClaimReviewRequest request,
            String adminEmail,
            String bearerToken
    ) {
        log.info("Admin {} reviewing claim id: {} decision: {}", adminEmail, claimId, request.getDecision());

        AuditAction action = request.getDecision().equals("APPROVED")
                ? AuditAction.APPROVE_CLAIM
                : AuditAction.REJECT_CLAIM;

        try {
            ClaimResponse claim =
                    claimsServiceClient.reviewClaim(claimId, request, bearerToken).getData();

            auditLogService.logSuccess(
                    adminEmail,
                    action,
                    "CLAIM",
                    claim.getClaimNumber(),
                    request.getDecision() + " claim: " +
                    claim.getClaimNumber() +
                    (request.getApprovedAmount() != null
                        ? " for amount: " + request.getApprovedAmount()
                        : "") +
                    ". Remarks: " + request.getAdminRemarks()
            );

            try {
                com.InsuranceManagementSystem.AdminService.dto.ClaimReviewedMessage message = new com.InsuranceManagementSystem.AdminService.dto.ClaimReviewedMessage(
                        claim.getId(),
                        claim.getClaimNumber(),
                        0L,
                        claim.getCustomerEmail(),
                        claim.getCustomerEmail(),
                        claim.getPolicyNumber(),
                        claim.getClaimedAmount().doubleValue(),
                        claim.getApprovedAmount() != null ? claim.getApprovedAmount().doubleValue() : 0.0,
                        request.getDecision(),
                        request.getAdminRemarks(),
                        java.time.LocalDateTime.now()
                );
                rabbitTemplate.convertAndSend(
                        com.InsuranceManagementSystem.AdminService.config.RabbitMQConfig.EXCHANGE, 
                        com.InsuranceManagementSystem.AdminService.config.RabbitMQConfig.CLAIM_REVIEWED_QUEUE, 
                        message
                );
            } catch (Exception e) {
                log.error("Failed to publish ClaimReviewedMessage for claim: {}", claim.getClaimNumber(), e);
            }

            return claim;

        } catch (Exception e) {
            auditLogService.logFailure(
                    adminEmail,
                    action,
                    "CLAIM",
                    claimId.toString(),
                    e.getMessage()
            );
            throw e;
        }
    }

    /**
     * Marks a previously approved claim as settled.
     *
     * @param claimId     The unique identifier of the claim.
     * @param adminEmail  The email of the administrator.
     * @param bearerToken The administrator's authorization token.
     * @return {@link ClaimResponse} containing the updated claim details.
     */
    public ClaimResponse settleClaim(
            Long claimId,
            String adminEmail,
            String bearerToken
    ) {
        log.info("Admin {} settling claim id: {}", adminEmail, claimId);

        try {
            ClaimResponse claim =
                    claimsServiceClient.settleClaim(claimId, bearerToken).getData();

            auditLogService.logSuccess(
                    adminEmail,
                    AuditAction.SETTLE_CLAIM,
                    "CLAIM",
                    claim.getClaimNumber(),
                    "Settled claim: " + claim.getClaimNumber() +
                    ". Amount: " + claim.getApprovedAmount()
            );

            return claim;

        } catch (Exception e) {
            auditLogService.logFailure(
                    adminEmail,
                    AuditAction.SETTLE_CLAIM,
                    "CLAIM",
                    claimId.toString(),
                    e.getMessage()
            );
            throw e;
        }
    }
}