package com.InsuranceManagementSystem.PolicyService.repository;

import com.InsuranceManagementSystem.PolicyService.entity.PolicyProduct;
import com.InsuranceManagementSystem.PolicyService.entity.PolicyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PolicyProduct entity.
 * Provides basic CRUD operations and custom queries for PolicyProducts.
 */
@Repository
public interface PolicyProductRepository extends JpaRepository<PolicyProduct, Long> {

    /**
     * Finds all active policy products.
     *
     * @return a list of active PolicyProduct objects
     */
    List<PolicyProduct> findByIsActiveTrue();

    /**
     * Finds all active policy products of a specific type.
     *
     * @param type the policy type to filter by
     * @return a list of active PolicyProduct objects matching the type
     */
    List<PolicyProduct> findByIsActiveTrueAndType(PolicyType type);

    /**
     * Finds all policy products of a specific type, regardless of their active status.
     *
     * @param type the policy type to filter by
     * @return a list of PolicyProduct objects matching the type
     */
    List<PolicyProduct> findByType(PolicyType type);

    /**
     * Checks if a policy product exists with the given name, ignoring case.
     *
     * @param name the name to check
     * @return true if a product with the name exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Checks if a policy product exists with the given name and type, ignoring case for the name.
     *
     * @param name the name to check
     * @param type the policy type to check
     * @return true if a product with the name and type exists, false otherwise
     */
    boolean existsByNameIgnoreCaseAndType(String name, PolicyType type);

    /**
     * Finds an active policy product by its ID.
     *
     * @param id the ID of the policy product
     * @return an Optional containing the active PolicyProduct if found
     */
    Optional<PolicyProduct> findByIdAndIsActiveTrue(Long id);

    /**
     * Finds all policy products created by a specific user.
     *
     * @param createdBy the username or email of the creator
     * @return a list of PolicyProduct objects created by the specified user
     */
    List<PolicyProduct> findByCreatedBy(String createdBy);
}
