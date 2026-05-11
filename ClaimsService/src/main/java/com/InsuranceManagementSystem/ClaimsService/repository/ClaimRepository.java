package com.InsuranceManagementSystem.ClaimsService.repository;

import com.InsuranceManagementSystem.ClaimsService.entity.Claim;
import com.InsuranceManagementSystem.ClaimsService.enums.ClaimStatus;
import com.InsuranceManagementSystem.ClaimsService.enums.ClaimType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByCustomerEmail(String customerEmail);

    List<Claim> findByCustomerEmailAndStatus(String customerEmail, ClaimStatus status);

    Optional<Claim> findByIdAndCustomerEmail(Long id, String customerEmail);

    Optional<Claim> findByClaimNumberAndCustomerEmail(String claimNumber, String customerEmail);

    List<Claim> findByPolicyNumber(String policyNumber);

    boolean existsByPolicyNumberAndStatusNotIn(String policyNumber, List<ClaimStatus> statuses);

    List<Claim> findByStatus(ClaimStatus status);

    List<Claim> findByClaimType(ClaimType claimType);

    Optional<Claim> findByClaimNumber(String claimNumber);

    List<Claim> findByReviewedBy(String adminEmail);

    @Query("SELECT c FROM Claim c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    List<Claim> findClaimsInDateRange(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c FROM Claim c WHERE c.status = :status AND c.createdAt BETWEEN :startDate AND :endDate")
    List<Claim> findClaimsByStatusInDateRange(@Param("status") ClaimStatus status,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(c.claimedAmount), 0) FROM Claim c WHERE c.customerEmail = :email")
    BigDecimal getTotalClaimedAmountByCustomer(@Param("email") String email);

    @Query("SELECT COALESCE(SUM(c.approvedAmount), 0) FROM Claim c WHERE c.customerEmail = :email AND c.status = 'SETTLED'")
    BigDecimal getTotalApprovedAmountByCustomer(@Param("email") String email);

    long countByStatus(ClaimStatus status);

    long countByCustomerEmail(String customerEmail);
}