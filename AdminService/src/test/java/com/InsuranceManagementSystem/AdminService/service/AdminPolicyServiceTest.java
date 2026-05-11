package com.InsuranceManagementSystem.AdminService.service;

import com.InsuranceManagementSystem.AdminService.dto.external.ExternalApiResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.PolicyProductRequest;
import com.InsuranceManagementSystem.AdminService.dto.external.PolicyProductResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.PurchasedPolicyResponse;
import com.InsuranceManagementSystem.AdminService.enums.AuditAction;
import com.InsuranceManagementSystem.AdminService.external.PolicyServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminPolicyServiceTest {

    @Mock
    private PolicyServiceClient policyServiceClient;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AdminPolicyService adminPolicyService;

    private PolicyProductRequest productRequest;
    private PolicyProductResponse productResponse;
    private PurchasedPolicyResponse purchasedPolicyResponse;
    private final String adminEmail = "admin@insurance.com";
    private final String bearerToken = "Bearer token";

    @BeforeEach
    void setUp() {
        productRequest = new PolicyProductRequest(
                "Health Shield", "HEALTH", "description", new BigDecimal("500000"), new BigDecimal("1000"), 12, 18, 60
        );

        productResponse = new PolicyProductResponse();
        productResponse.setId(1L);
        productResponse.setName("Health Shield");
        productResponse.setDescription("description");
        productResponse.setType("HEALTH");
        productResponse.setCoverageAmount(new BigDecimal("500000"));
        productResponse.setBasePremium(new BigDecimal("1000"));
        productResponse.setIsActive(true);

        purchasedPolicyResponse = new PurchasedPolicyResponse();
        purchasedPolicyResponse.setId(1L);
        purchasedPolicyResponse.setPolicyNumber("POL-123");
        purchasedPolicyResponse.setProductName("Health Shield");
        purchasedPolicyResponse.setCustomerEmail("customer@test.com");
    }

    private <T> ExternalApiResponse<T> createSuccessResponse(T data) {
        ExternalApiResponse<T> res = new ExternalApiResponse<>();
        res.setSuccess(true);
        res.setMessage("Success");
        res.setData(data);
        return res;
    }

    @Test
    @DisplayName("Should create policy product and log success")
    void createProduct_ShouldReturnProductAndLog() {
        ExternalApiResponse<PolicyProductResponse> response = createSuccessResponse(productResponse);
        when(policyServiceClient.createProduct(any(PolicyProductRequest.class), eq(bearerToken))).thenReturn(response);

        PolicyProductResponse result = adminPolicyService.createProduct(productRequest, adminEmail, bearerToken);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.CREATE_POLICY_PRODUCT), eq("POLICY_PRODUCT"), eq("1"), anyString());
    }

    @Test
    @DisplayName("Should throw exception and log failure when create fails")
    void createProduct_WhenException_ShouldLogFailure() {
        when(policyServiceClient.createProduct(any(), eq(bearerToken))).thenThrow(new RuntimeException("Service unavailable"));

        assertThatThrownBy(() -> adminPolicyService.createProduct(productRequest, adminEmail, bearerToken))
                .isInstanceOf(RuntimeException.class);

        verify(auditLogService).logFailure(eq(adminEmail), eq(AuditAction.CREATE_POLICY_PRODUCT), eq("POLICY_PRODUCT"), eq("N/A"), anyString());
    }

    @Test
    @DisplayName("Should update policy product and log success")
    void updateProduct_ShouldReturnProductAndLog() {
        ExternalApiResponse<PolicyProductResponse> response = createSuccessResponse(productResponse);
        when(policyServiceClient.updateProduct(eq(1L), any(PolicyProductRequest.class), eq(bearerToken))).thenReturn(response);

        PolicyProductResponse result = adminPolicyService.updateProduct(1L, productRequest, adminEmail, bearerToken);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.UPDATE_POLICY_PRODUCT), eq("POLICY_PRODUCT"), eq("1"), anyString());
    }

    @Test
    @DisplayName("Should throw exception and log failure when update fails")
    void updateProduct_WhenException_ShouldLogFailure() {
        when(policyServiceClient.updateProduct(eq(1L), any(), eq(bearerToken))).thenThrow(new RuntimeException("Service unavailable"));

        assertThatThrownBy(() -> adminPolicyService.updateProduct(1L, productRequest, adminEmail, bearerToken))
                .isInstanceOf(RuntimeException.class);

        verify(auditLogService).logFailure(eq(adminEmail), eq(AuditAction.UPDATE_POLICY_PRODUCT), eq("POLICY_PRODUCT"), eq("1"), anyString());
    }

    @Test
    @DisplayName("Should deactivate policy product and log success")
    void deactivateProduct_ShouldLogSuccess() {
        ExternalApiResponse<Void> response = createSuccessResponse(null);
        when(policyServiceClient.deactivateProduct(1L, bearerToken)).thenReturn(response);

        adminPolicyService.deactivateProduct(1L, adminEmail, bearerToken);

        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.DEACTIVATE_POLICY_PRODUCT), eq("POLICY_PRODUCT"), eq("1"), anyString());
    }

    @Test
    @DisplayName("Should log failure when deactivate fails")
    void deactivateProduct_WhenException_ShouldLogFailure() {
        when(policyServiceClient.deactivateProduct(1L, bearerToken)).thenThrow(new RuntimeException("Error"));

        assertThatThrownBy(() -> adminPolicyService.deactivateProduct(1L, adminEmail, bearerToken))
                .isInstanceOf(RuntimeException.class);

        verify(auditLogService).logFailure(eq(adminEmail), eq(AuditAction.DEACTIVATE_POLICY_PRODUCT), eq("POLICY_PRODUCT"), eq("1"), anyString());
    }

    @Test
    @DisplayName("Should reactivate policy product and log success")
    void reactivateProduct_ShouldLogSuccess() {
        ExternalApiResponse<Void> response = createSuccessResponse(null);
        when(policyServiceClient.reactivateProduct(1L, bearerToken)).thenReturn(response);

        adminPolicyService.reactivateProduct(1L, adminEmail, bearerToken);

        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.REACTIVATE_POLICY_PRODUCT), eq("POLICY_PRODUCT"), eq("1"), anyString());
    }

    @Test
    @DisplayName("Should log failure when reactivate fails")
    void reactivateProduct_WhenException_ShouldLogFailure() {
        when(policyServiceClient.reactivateProduct(1L, bearerToken)).thenThrow(new RuntimeException("Error"));

        assertThatThrownBy(() -> adminPolicyService.reactivateProduct(1L, adminEmail, bearerToken))
                .isInstanceOf(RuntimeException.class);

        verify(auditLogService).logFailure(eq(adminEmail), eq(AuditAction.REACTIVATE_POLICY_PRODUCT), eq("POLICY_PRODUCT"), eq("1"), anyString());
    }

    @Test
    @DisplayName("Should fetch all products and log success")
    void getAllProducts_ShouldReturnListAndLog() {
        ExternalApiResponse<List<PolicyProductResponse>> response = createSuccessResponse(List.of(productResponse));
        when(policyServiceClient.getAllProducts(bearerToken)).thenReturn(response);

        List<PolicyProductResponse> result = adminPolicyService.getAllProducts(adminEmail, bearerToken);

        assertThat(result).hasSize(1);
        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.VIEW_ALL_POLICY_PRODUCTS), eq("POLICY_PRODUCT"), eq("ALL"), anyString());
    }

    @Test
    @DisplayName("Should fetch product by id and log success")
    void getProductById_ShouldReturnProductAndLog() {
        ExternalApiResponse<PolicyProductResponse> response = createSuccessResponse(productResponse);
        when(policyServiceClient.getProductById(1L, bearerToken)).thenReturn(response);

        PolicyProductResponse result = adminPolicyService.getProductById(1L, adminEmail, bearerToken);

        assertThat(result).isNotNull();
        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.VIEW_POLICY_PRODUCT), eq("POLICY_PRODUCT"), eq("1"), anyString());
    }

    @Test
    @DisplayName("Should fetch all purchased policies without audit log")
    void getAllPurchasedPolicies_ShouldReturnList() {
        ExternalApiResponse<List<PurchasedPolicyResponse>> response = createSuccessResponse(List.of(purchasedPolicyResponse));
        when(policyServiceClient.getAllPurchasedPolicies(bearerToken)).thenReturn(response);

        List<PurchasedPolicyResponse> result = adminPolicyService.getAllPurchasedPolicies(adminEmail, bearerToken);

        assertThat(result).hasSize(1);
        verifyNoInteractions(auditLogService);
    }
}
