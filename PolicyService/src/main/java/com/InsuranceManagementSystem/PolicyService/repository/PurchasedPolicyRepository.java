package com.InsuranceManagementSystem.PolicyService.repository;

import com.InsuranceManagementSystem.PolicyService.entity.PurchasedPolicy;
import com.InsuranceManagementSystem.PolicyService.entity.PolicyStatus;
import com.InsuranceManagementSystem.PolicyService.entity.PolicyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PurchasedPolicy entity.
 * Provides basic CRUD operations and custom queries for PurchasedPolicies.
 */
@Repository
public interface PurchasedPolicyRepository extends JpaRepository<PurchasedPolicy, Long> {

    /**
     * Finds all policies purchased by a specific customer.
     *
     * @param customerEmail the email address of the customer
     * @return a list of PurchasedPolicy objects
     */
    List<PurchasedPolicy> findByCustomerEmail(String customerEmail);

    /**
     * Finds all policies for a customer with a specific status.
     *
     * @param customerEmail the email address of the customer
     * @param status the status to filter by (e.g., ACTIVE, EXPIRED)
     * @return a list of matching PurchasedPolicy objects
     */
    List<PurchasedPolicy> findByCustomerEmailAndStatus(String customerEmail, PolicyStatus status);

    /**
     * Finds a purchased policy by its unique policy number.
     *
     * @param policyNumber the unique policy number
     * @return an Optional containing the PurchasedPolicy if found
     */
    Optional<PurchasedPolicy> findByPolicyNumber(String policyNumber);

    /**
     * Finds a specific policy by its ID, ensuring it belongs to the specified customer.
     *
     * @param id the ID of the purchased policy
     * @param customerEmail the email address of the customer
     * @return an Optional containing the PurchasedPolicy if found
     */
    Optional<PurchasedPolicy> findByIdAndCustomerEmail(Long id, String customerEmail);

    /**
     * Finds all purchased policies with a specific status.
     *
     * @param status the status to filter by
     * @return a list of matching PurchasedPolicy objects
     */
    List<PurchasedPolicy> findByStatus(PolicyStatus status);

    /**
     * Finds all instances of a specific policy product being purchased.
     *
     * @param productId the ID of the policy product
     * @return a list of matching PurchasedPolicy objects
     */
    List<PurchasedPolicy> findByProductId(Long productId);

    /**
     * Finds all policies for a customer of a specific policy type.
     *
     * @param customerEmail the email address of the customer
     * @param policyType the type of policy (e.g., HEALTH, VEHICLE)
     * @return a list of matching PurchasedPolicy objects
     */
    List<PurchasedPolicy> findByCustomerEmailAndPolicyType(String customerEmail, PolicyType policyType);

    /**
     * Checks if a customer already has a policy of a specific type with a specific status.
     *
     * @param customerEmail the email address of the customer
     * @param policyType the type of policy
     * @param status the status of the policy
     * @return true if such a policy exists, false otherwise
     */
    boolean existsByCustomerEmailAndPolicyTypeAndStatus(String customerEmail, PolicyType policyType, PolicyStatus status);

    /**
     * Counts the total number of times a specific policy product has been purchased.
     *
     * @param productId the ID of the policy product
     * @return the total count
     */
    long countByProductId(Long productId);

    /**
     * Finds all active policies that have passed their end date.
     *
     * @param today the current date
     * @return a list of expired PurchasedPolicy objects
     */
    @Query("SELECT p FROM PurchasedPolicy p WHERE p.endDate < :today AND p.status = 'ACTIVE'")
    List<PurchasedPolicy> findExpiredPolicies(@Param("today") LocalDate today);

    /**
     * Finds all active policies that will expire between the current date and a future date.
     *
     * @param today the current date
     * @param futureDate the date up to which to check for expirations
     * @return a list of PurchasedPolicy objects expiring soon
     */
    @Query("SELECT p FROM PurchasedPolicy p WHERE p.endDate BETWEEN :today AND :futureDate AND p.status = 'ACTIVE'")
    List<PurchasedPolicy> findPoliciesExpiringSoon(@Param("today") LocalDate today, @Param("futureDate") LocalDate futureDate);
}