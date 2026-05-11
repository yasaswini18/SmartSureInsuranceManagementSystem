package com.InsuranceManagementSystem.PolicyService.service;

import com.InsuranceManagementSystem.PolicyService.dto.PolicyProductRequest;
import com.InsuranceManagementSystem.PolicyService.dto.PolicyProductResponse;
import com.InsuranceManagementSystem.PolicyService.entity.PolicyProduct;
import com.InsuranceManagementSystem.PolicyService.entity.PolicyType;
import com.InsuranceManagementSystem.PolicyService.repository.PolicyProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyProductServiceTest {

    @Mock
    private PolicyProductRepository productRepository;

    @InjectMocks
    private PolicyProductService productService;

    private PolicyProductRequest request;
    private PolicyProduct mockProduct;

    @BeforeEach
    void setUp() {
        request = new PolicyProductRequest(
                "Health Shield Basic",
                PolicyType.HEALTH,
                "Comprehensive health insurance coverage",
                new BigDecimal("5000.00"),
                new BigDecimal("500000.00"),
                12,
                18,
                65
        );

        mockProduct = PolicyProduct.builder()
                .id(1L)
                .name("Health Shield Basic")
                .type(PolicyType.HEALTH)
                .description("Comprehensive health insurance coverage")
                .basePremium(new BigDecimal("5000.00"))
                .coverageAmount(new BigDecimal("500000.00"))
                .durationMonths(12)
                .minAge(18)
                .maxAge(65)
                .isActive(true)
                .createdBy("admin@insurance.com")
                .build();
    }

    @Test
    @DisplayName("Should create product successfully")
    void createProduct_WithValidRequest_ShouldReturnResponse() {
        when(productRepository.existsByNameIgnoreCase("Health Shield Basic")).thenReturn(false);
        when(productRepository.save(any(PolicyProduct.class))).thenReturn(mockProduct);

        PolicyProductResponse response = productService.createProduct(request, "admin@insurance.com");

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Health Shield Basic");
        verify(productRepository).save(any(PolicyProduct.class));
    }

    @Test
    @DisplayName("Should throw exception if min age > max age on create")
    void createProduct_WithInvalidAge_ShouldThrowException() {
        request.setMinAge(70);
        assertThatThrownBy(() -> productService.createProduct(request, "admin@insurance.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Minimum age cannot be greater than maximum age");
    }

    @Test
    @DisplayName("Should throw exception for duplicate product name on create")
    void createProduct_WithDuplicateName_ShouldThrowException() {
        when(productRepository.existsByNameIgnoreCase("Health Shield Basic")).thenReturn(true);
        assertThatThrownBy(() -> productService.createProduct(request, "admin@insurance.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should update product successfully")
    void updateProduct_WithValidRequest_ShouldReturnResponse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(productRepository.save(any(PolicyProduct.class))).thenReturn(mockProduct);

        PolicyProductResponse response = productService.updateProduct(1L, request, "admin@insurance.com");

        assertThat(response.getName()).isEqualTo("Health Shield Basic");
    }

    @Test
    @DisplayName("Should throw exception if product not found on update")
    void updateProduct_NotFound_ShouldThrowException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> productService.updateProduct(1L, request, "admin@insurance.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should throw exception if min age > max age on update")
    void updateProduct_InvalidAge_ShouldThrowException() {
        request.setMinAge(70);
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        assertThatThrownBy(() -> productService.updateProduct(1L, request, "admin@insurance.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should throw exception if duplicate name on update")
    void updateProduct_DuplicateName_ShouldThrowException() {
        request.setName("Another Policy");
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(productRepository.existsByNameIgnoreCase("Another Policy")).thenReturn(true);
        assertThatThrownBy(() -> productService.updateProduct(1L, request, "admin@insurance.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should deactivate product successfully")
    void deactivateProduct_WithActiveProduct_ShouldDeactivate() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        productService.deactivateProduct(1L, "admin@insurance.com");
        verify(productRepository).save(argThat(p -> !p.getIsActive()));
    }

    @Test
    @DisplayName("Should throw exception deactivating inactive product")
    void deactivateProduct_AlreadyInactive_ShouldThrowException() {
        mockProduct.setIsActive(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        assertThatThrownBy(() -> productService.deactivateProduct(1L, "admin@insurance.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should throw exception deactivating not found product")
    void deactivateProduct_NotFound_ShouldThrowException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> productService.deactivateProduct(1L, "admin@insurance.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should reactivate product successfully")
    void reactivateProduct_WithInactiveProduct_ShouldReactivate() {
        mockProduct.setIsActive(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        productService.reactivateProduct(1L, "admin@insurance.com");
        verify(productRepository).save(argThat(p -> p.getIsActive()));
    }

    @Test
    @DisplayName("Should throw exception reactivating active product")
    void reactivateProduct_AlreadyActive_ShouldThrowException() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        assertThatThrownBy(() -> productService.reactivateProduct(1L, "admin@insurance.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should get all active products")
    void getAllActiveProducts_ShouldReturnActiveOnly() {
        when(productRepository.findByIsActiveTrue()).thenReturn(List.of(mockProduct));
        List<PolicyProductResponse> products = productService.getAllActiveProducts();
        assertThat(products).hasSize(1);
    }

    @Test
    @DisplayName("Should get all products")
    void getAllProducts_ShouldReturnAll() {
        when(productRepository.findAll()).thenReturn(List.of(mockProduct));
        List<PolicyProductResponse> products = productService.getAllProducts();
        assertThat(products).hasSize(1);
    }

    @Test
    @DisplayName("Should get product by id")
    void getProductById_WithValidId_ShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        PolicyProductResponse response = productService.getProductById(1L);
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void getProductById_WithInvalidId_ShouldThrowException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should get active products by type")
    void getProductsByType_ShouldReturnProducts() {
        when(productRepository.findByIsActiveTrueAndType(PolicyType.HEALTH)).thenReturn(List.of(mockProduct));
        List<PolicyProductResponse> products = productService.getProductsByType(PolicyType.HEALTH);
        assertThat(products).hasSize(1);
    }
}
