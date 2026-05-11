package com.InsuranceManagementSystem.PolicyService.service;

import com.InsuranceManagementSystem.PolicyService.dto.PolicyProductRequest;
import com.InsuranceManagementSystem.PolicyService.dto.PolicyProductResponse;
import com.InsuranceManagementSystem.PolicyService.entity.PolicyProduct;
import com.InsuranceManagementSystem.PolicyService.entity.PolicyType;
import com.InsuranceManagementSystem.PolicyService.repository.PolicyProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing policy products.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyProductService {

    private final PolicyProductRepository productRepository;

    /**
     * Creates a new policy product.
     *
     * @param request the details of the policy product to create
     * @param adminEmail the email of the admin creating the product
     * @return the created policy product as a response object
     */
    public PolicyProductResponse createProduct(
            PolicyProductRequest request,
            String adminEmail
    ) {
        log.info("Admin {} creating new policy product: {}", adminEmail, request.getName());

        if (request.getMinAge() > request.getMaxAge()) {
            throw new RuntimeException("Minimum age cannot be greater than maximum age");
        }

        if (productRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException(
                "Policy product with name '" + request.getName() + "' already exists"
            );
        }

        PolicyProduct product = PolicyProduct.builder()
                .name(request.getName())
                .type(request.getType())
                .description(request.getDescription())
                .basePremium(request.getBasePremium())
                .coverageAmount(request.getCoverageAmount())
                .durationMonths(request.getDurationMonths())
                .minAge(request.getMinAge())
                .maxAge(request.getMaxAge())
                .isActive(true)
                .createdBy(adminEmail)
                .build();

        PolicyProduct saved = productRepository.save(product);
        log.info("Policy product created with id: {}", saved.getId());

        return mapToProductResponse(saved);
    }

    /**
     * Updates an existing policy product.
     *
     * @param productId the ID of the policy product to update
     * @param request the updated details of the policy product
     * @param adminEmail the email of the admin updating the product
     * @return the updated policy product as a response object
     */
    public PolicyProductResponse updateProduct(
            Long productId,
            PolicyProductRequest request,
            String adminEmail
    ) {
        log.info("Admin {} updating policy product id: {}", adminEmail, productId);

        PolicyProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException(
                    "Policy product not found with id: " + productId
                ));

        if (request.getMinAge() > request.getMaxAge()) {
            throw new RuntimeException("Minimum age cannot be greater than maximum age");
        }

        if (!product.getName().equals(request.getName()) &&
             productRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException(
                "Policy product with name '" + request.getName() + "' already exists"
            );
        }

        product.setName(request.getName());
        product.setType(request.getType());
        product.setDescription(request.getDescription());
        product.setBasePremium(request.getBasePremium());
        product.setCoverageAmount(request.getCoverageAmount());
        product.setDurationMonths(request.getDurationMonths());
        product.setMinAge(request.getMinAge());
        product.setMaxAge(request.getMaxAge());

        PolicyProduct updated = productRepository.save(product);
        log.info("Policy product updated successfully: {}", productId);

        return mapToProductResponse(updated);
    }

    /**
     * Deactivates a policy product.
     *
     * @param productId the ID of the policy product to deactivate
     * @param adminEmail the email of the admin deactivating the product
     */
    public void deactivateProduct(Long productId, String adminEmail) {
        log.info("Admin {} deactivating policy product id: {}", adminEmail, productId);

        PolicyProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException(
                    "Policy product not found with id: " + productId
                ));

        if (!product.getIsActive()) {
            throw new RuntimeException(
                "Policy product is already inactive: " + productId
            );
        }

        product.setIsActive(false);
        productRepository.save(product);

        log.info("Policy product deactivated: {}", productId);
    }

    /**
     * Reactivates a deactivated policy product.
     *
     * @param productId the ID of the policy product to reactivate
     * @param adminEmail the email of the admin reactivating the product
     */
    public void reactivateProduct(Long productId, String adminEmail) {
        log.info("Admin {} reactivating policy product id: {}", adminEmail, productId);

        PolicyProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException(
                    "Policy product not found with id: " + productId
                ));

        if (product.getIsActive()) {
            throw new RuntimeException(
                "Policy product is already active: " + productId
            );
        }

        product.setIsActive(true);
        productRepository.save(product);

        log.info("Policy product reactivated: {}", productId);
    }

    /**
     * Retrieves all active policy products.
     *
     * @return a list of active policy products
     */
    public List<PolicyProductResponse> getAllActiveProducts() {
        log.info("Fetching all active policy products");

        return productRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all policy products, including inactive ones.
     *
     * @return a list of all policy products
     */
    public List<PolicyProductResponse> getAllProducts() {
        log.info("Admin fetching all policy products");

        return productRepository.findAll()
                .stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a policy product by its ID.
     *
     * @param productId the ID of the policy product
     * @return the requested policy product as a response object
     */
    public PolicyProductResponse getProductById(Long productId) {
        log.info("Fetching policy product by id: {}", productId);

        PolicyProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException(
                    "Policy product not found with id: " + productId
                ));

        return mapToProductResponse(product);
    }

    /**
     * Retrieves active policy products filtered by type.
     *
     * @param type the type of policy product to filter by
     * @return a list of active policy products of the specified type
     */
    public List<PolicyProductResponse> getProductsByType(PolicyType type) {
        log.info("Fetching active policy products by type: {}", type);

        return productRepository.findByIsActiveTrueAndType(type)
                .stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * Maps a PolicyProduct entity to a PolicyProductResponse DTO.
     *
     * @param product the PolicyProduct entity
     * @return the mapped PolicyProductResponse DTO
     */
    public PolicyProductResponse mapToProductResponse(PolicyProduct product) {
        return PolicyProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .type(product.getType())
                .description(product.getDescription())
                .basePremium(product.getBasePremium())
                .coverageAmount(product.getCoverageAmount())
                .durationMonths(product.getDurationMonths())
                .minAge(product.getMinAge())
                .maxAge(product.getMaxAge())
                .isActive(product.getIsActive())
                .createdBy(product.getCreatedBy())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
