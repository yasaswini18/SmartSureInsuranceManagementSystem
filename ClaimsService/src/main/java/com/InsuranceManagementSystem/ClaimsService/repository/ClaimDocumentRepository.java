package com.InsuranceManagementSystem.ClaimsService.repository;

import com.InsuranceManagementSystem.ClaimsService.entity.ClaimDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClaimDocumentRepository extends JpaRepository<ClaimDocument, Long> {

    List<ClaimDocument> findByClaimId(Long claimId);

    Optional<ClaimDocument> findByIdAndClaimId(Long id, Long claimId);

    long countByClaimId(Long claimId);

    boolean existsByClaimIdAndFileName(Long claimId, String fileName);

    @Query("SELECT COALESCE(SUM(d.fileSize), 0) FROM ClaimDocument d WHERE d.claim.id = :claimId")
    Long getTotalFileSizeByClaimId(@Param("claimId") Long claimId);

    void deleteByClaimId(Long claimId);
}