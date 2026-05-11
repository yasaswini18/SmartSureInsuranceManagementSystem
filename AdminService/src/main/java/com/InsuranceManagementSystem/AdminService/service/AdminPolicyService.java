package com.InsuranceManagementSystem.AdminService.service;

import com.InsuranceManagementSystem.AdminService.dto.external.PolicyProductRequest;
import com.InsuranceManagementSystem.AdminService.dto.external.PolicyProductResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.PurchasedPolicyResponse;
import com.InsuranceManagementSystem.AdminService.enums.AuditAction;
import com.InsuranceManagementSystem.AdminService.external.PolicyServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing insurance policies and products from the
 * administrative perspective. Orchestrates communication with the
 * PolicyService and records audit logs for administrative actions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminPolicyService {

    private final PolicyServiceClient policyServiceClient;
    private final AuditLogService auditLogService;

    /**
     * Creates a new policy product.
     *
     * @param request     The request containing product details.
     * @param adminEmail  The email of the administrator.
     * @param bearerToken The administrator's authorization token.
     * @return {@link PolicyProductResponse} representing the created product.
     */
    public PolicyProductResponse createProduct(
            PolicyProductRequest request,
            String adminEmail,
            String bearerToken
    ) {
        log.info("Admin {} creating policy product: {}", adminEmail, request.getName());

        try {
            PolicyProductResponse response =
                    policyServiceClient.createProduct(request, bearerToken).getData();

            auditLogService.logSuccess(
                    adminEmail,
                    AuditAction.CREATE_POLICY_PRODUCT,
                    "POLICY_PRODUCT",
                    response.getId().toString(),
                    "Created policy product: " + request.getName()
            );

            return response;

        } catch (Exception e) {
            auditLogService.logFailure(
                    adminEmail,
                    AuditAction.CREATE_POLICY_PRODUCT,
                    "POLICY_PRODUCT",
                    "N/A",
                    e.getMessage()
            );
            throw e;
        }
    }

    /**
     * Updates an existing policy product.
     *
     * @param productId   The unique identifier of the product.
     * @param request     The updated product details.
     * @param adminEmail  The email of the administrator.
     * @param bearerToken The administrator's authorization token.
     * @return {@link PolicyProductResponse} representing the updated product.
     */
    public PolicyProductResponse updateProduct(
            Long productId,
            PolicyProductRequest request,
            String adminEmail,
            String bearerToken
    ) {
        log.info("Admin {} updating policy product id: {}", adminEmail, productId);

        try {
            PolicyProductResponse response =
                    policyServiceClient.updateProduct(productId, request, bearerToken).getData();

            auditLogService.logSuccess(
                    adminEmail,
                    AuditAction.UPDATE_POLICY_PRODUCT,
                    "POLICY_PRODUCT",
                    productId.toString(),
                    "Updated policy product: " + request.getName()
            );

            return response;

        } catch (Exception e) {
            auditLogService.logFailure(
                    adminEmail,
                    AuditAction.UPDATE_POLICY_PRODUCT,
                    "POLICY_PRODUCT",
                    productId.toString(),
                    e.getMessage()
            );
            throw e;
        }
    }

    /**
     * Deactivates an active policy product.
     *
     * @param productId   The unique identifier of the product.
     * @param adminEmail  The email of the administrator.
     * @param bearerToken The administrator's authorization token.
     */
    public void deactivateProduct(
            Long productId,
            String adminEmail,
            String bearerToken
    ) {
        log.info("Admin {} deactivating product id: {}", adminEmail, productId);

        try {
            policyServiceClient.deactivateProduct(productId, bearerToken);

            auditLogService.logSuccess(
                    adminEmail,
                    AuditAction.DEACTIVATE_POLICY_PRODUCT,
                    "POLICY_PRODUCT",
                    productId.toString(),
                    "Deactivated policy product id: " + productId
            );

        } catch (Exception e) {
            auditLogService.logFailure(
                    adminEmail,
                    AuditAction.DEACTIVATE_POLICY_PRODUCT,
                    "POLICY_PRODUCT",
                    productId.toString(),
                    e.getMessage()
            );
            throw e;
        }
    }

    /**
     * Reactivates an inactive policy product.
     *
     * @param productId   The unique identifier of the product.
     * @param adminEmail  The email of the administrator.
     * @param bearerToken The administrator's authorization token.
     */
    public void reactivateProduct(
            Long productId,
            String adminEmail,
            String bearerToken
    ) {
        log.info("Admin {} reactivating product id: {}", adminEmail, productId);

        try {
            policyServiceClient.reactivateProduct(productId, bearerToken);

            auditLogService.logSuccess(
                    adminEmail,
                    AuditAction.REACTIVATE_POLICY_PRODUCT,
                    "POLICY_PRODUCT",
                    productId.toString(),
                    "Reactivated policy product id: " + productId
            );

        } catch (Exception e) {
            auditLogService.logFailure(
                    adminEmail,
                    AuditAction.REACTIVATE_POLICY_PRODUCT,
                    "POLICY_PRODUCT",
                    productId.toString(),
                    e.getMessage()
            );
            throw e;
        }
    }

    /**
     * Retrieves all policy products.
     *
     * @param adminEmail  The email of the administrator.
     * @param bearerToken The administrator's authorization token.
     * @return List of {@link PolicyProductResponse} representing all products.
     */
    public List<PolicyProductResponse> getAllProducts(
            String adminEmail,
            String bearerToken
    ) {
        log.info("Admin {} fetching all products", adminEmail);

        List<PolicyProductResponse> products =
                policyServiceClient.getAllProducts(bearerToken).getData();

        auditLogService.logSuccess(
                adminEmail,
                AuditAction.VIEW_ALL_POLICY_PRODUCTS,
                "POLICY_PRODUCT",
                "ALL",
                "Viewed all policy products. Count: " + products.size()
        );

        return products;
    }

    /**
     * Retrieves detailed information for a specific policy product.
     *
     * @param productId   The unique identifier of the product.
     * @param adminEmail  The email of the administrator.
     * @param bearerToken The administrator's authorization token.
     * @return {@link PolicyProductResponse} containing product details.
     */
    public PolicyProductResponse getProductById(
            Long productId,
            String adminEmail,
            String bearerToken
    ) {
        log.info("Admin {} fetching product id: {}", adminEmail, productId);

        PolicyProductResponse product =
                policyServiceClient.getProductById(productId, bearerToken).getData();

        auditLogService.logSuccess(
                adminEmail,
                AuditAction.VIEW_POLICY_PRODUCT,
                "POLICY_PRODUCT",
                productId.toString(),
                "Viewed policy product: " + product.getName()
        );

        return product;
    }

    /**
     * Retrieves all purchased policies in the system.
     *
     * @param adminEmail  The email of the administrator.
     * @param bearerToken The administrator's authorization token.
     * @return List of {@link PurchasedPolicyResponse} representing all purchased policies.
     */
    public List<PurchasedPolicyResponse> getAllPurchasedPolicies(
            String adminEmail,
            String bearerToken
    ) {
        log.info("Admin {} fetching all purchased policies", adminEmail);

        return policyServiceClient.getAllPurchasedPolicies(bearerToken).getData();
    }
}