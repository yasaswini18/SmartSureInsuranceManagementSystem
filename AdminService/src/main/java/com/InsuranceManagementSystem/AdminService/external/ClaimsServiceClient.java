package com.InsuranceManagementSystem.AdminService.external;

import com.InsuranceManagementSystem.AdminService.config.OpenFeignConfig;
import com.InsuranceManagementSystem.AdminService.dto.external.ClaimResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.ClaimReviewRequest;
import com.InsuranceManagementSystem.AdminService.dto.external.ClaimSummaryResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.ExternalApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "ClaimsService",
//        url = "${claims.service.url}",
        configuration = OpenFeignConfig.class
)
public interface ClaimsServiceClient {

    @GetMapping("/api/claims/all")
    ExternalApiResponse<List<ClaimSummaryResponse>> getAllClaims(
            @RequestHeader("Authorization") String bearerToken
    );

    @GetMapping("/api/claims/pending")
    ExternalApiResponse<List<ClaimSummaryResponse>> getPendingClaims(
            @RequestHeader("Authorization") String bearerToken
    );

    @GetMapping("/api/claims/{claimId}")
    ExternalApiResponse<ClaimResponse> getClaimById(
            @PathVariable("claimId") Long claimId,
            @RequestHeader("Authorization") String bearerToken
    );

    @PutMapping("/api/claims/{claimId}/start-review")
    ExternalApiResponse<ClaimResponse> startReview(
            @PathVariable("claimId") Long claimId,
            @RequestHeader("Authorization") String bearerToken
    );

    @PutMapping("/api/claims/{claimId}/review")
    ExternalApiResponse<ClaimResponse> reviewClaim(
            @PathVariable("claimId") Long claimId,
            @RequestBody ClaimReviewRequest request,
            @RequestHeader("Authorization") String bearerToken
    );

    @PutMapping("/api/claims/{claimId}/settle")
    ExternalApiResponse<ClaimResponse> settleClaim(
            @PathVariable("claimId") Long claimId,
            @RequestHeader("Authorization") String bearerToken
    );
}