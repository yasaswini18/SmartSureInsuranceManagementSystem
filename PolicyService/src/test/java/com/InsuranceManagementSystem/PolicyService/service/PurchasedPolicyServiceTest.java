package com.InsuranceManagementSystem.PolicyService.service;

import com.InsuranceManagementSystem.PolicyService.dto.PolicyValidationResponse;
import com.InsuranceManagementSystem.PolicyService.dto.PurchasePolicyRequest;
import com.InsuranceManagementSystem.PolicyService.dto.PurchasedPolicyResponse;
import com.InsuranceManagementSystem.PolicyService.entity.PolicyProduct;
import com.InsuranceManagementSystem.PolicyService.entity.PolicyStatus;
import com.InsuranceManagementSystem.PolicyService.entity.PolicyType;
import com.InsuranceManagementSystem.PolicyService.entity.PurchasedPolicy;
import com.InsuranceManagementSystem.PolicyService.repository.PolicyProductRepository;
import com.InsuranceManagementSystem.PolicyService.repository.PurchasedPolicyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchasedPolicyServiceTest {

    @Mock
    private PurchasedPolicyRepository purchasedPolicyRepository;

    @Mock
    private PolicyProductRepository productRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PurchasedPolicyService purchasedPolicyService;

    private PolicyProduct product;
    private PurchasedPolicy purchasedPolicy;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(purchasedPolicyService, "policyNumberPrefix", "POL");

        product = PolicyProduct.builder()
                .id(1L)
                .name("Health Basic")
                .type(PolicyType.HEALTH)
                .basePremium(new BigDecimal("5000.00"))
                .coverageAmount(new BigDecimal("500000.00"))
                .durationMonths(12)
                .minAge(18)
                .maxAge(65)
                .isActive(true)
                .build();

        purchasedPolicy = PurchasedPolicy.builder()
                .id(100L)
                .policyNumber("POL-2026-000001")
                .customerEmail("test@gmail.com")
                .productId(1L)
                .productName("Health Basic")
                .policyType(PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("500000.00"))
                .premiumPaid(new BigDecimal("5750.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(12))
                .status(PolicyStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Should purchase policy successfully (Age < 25, coverage <= 500000)")
    void purchasePolicy_WithAgeUnder25_ShouldReturnResponse() {
        PurchasePolicyRequest request = new PurchasePolicyRequest(1L, null, 20, null);
        
        when(productRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(product));
        when(purchasedPolicyRepository.existsByCustomerEmailAndPolicyTypeAndStatus(
                "test@gmail.com", PolicyType.HEALTH, PolicyStatus.ACTIVE)).thenReturn(false);
        when(purchasedPolicyRepository.count()).thenReturn(0L);
        when(purchasedPolicyRepository.save(any(PurchasedPolicy.class))).thenReturn(purchasedPolicy);

        PurchasedPolicyResponse response = purchasedPolicyService.purchasePolicy(request, "test@gmail.com");

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Should purchase policy successfully (Age < 35, coverage <= 100000)")
    void purchasePolicy_WithAgeUnder35_CoverageUnder100k() {
        product.setCoverageAmount(new BigDecimal("50000.00"));
        PurchasePolicyRequest request = new PurchasePolicyRequest(1L, null, 30, null);
        
        when(productRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(product));
        when(purchasedPolicyRepository.existsByCustomerEmailAndPolicyTypeAndStatus(anyString(), any(), any())).thenReturn(false);
        when(purchasedPolicyRepository.save(any(PurchasedPolicy.class))).thenReturn(purchasedPolicy);

        PurchasedPolicyResponse response = purchasedPolicyService.purchasePolicy(request, "test@gmail.com");
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Should purchase policy successfully (Age < 45, coverage > 500000)")
    void purchasePolicy_WithAgeUnder45_CoverageOver500k() {
        product.setCoverageAmount(new BigDecimal("1000000.00"));
        PurchasePolicyRequest request = new PurchasePolicyRequest(1L, null, 40, null);
        
        when(productRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(product));
        when(purchasedPolicyRepository.existsByCustomerEmailAndPolicyTypeAndStatus(anyString(), any(), any())).thenReturn(false);
        when(purchasedPolicyRepository.save(any(PurchasedPolicy.class))).thenReturn(purchasedPolicy);

        PurchasedPolicyResponse response = purchasedPolicyService.purchasePolicy(request, "test@gmail.com");
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Should purchase policy successfully (Age < 55)")
    void purchasePolicy_WithAgeUnder55() {
        PurchasePolicyRequest request = new PurchasePolicyRequest(1L, null, 50, null);
        
        when(productRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(product));
        when(purchasedPolicyRepository.existsByCustomerEmailAndPolicyTypeAndStatus(anyString(), any(), any())).thenReturn(false);
        when(purchasedPolicyRepository.save(any(PurchasedPolicy.class))).thenReturn(purchasedPolicy);

        purchasedPolicyService.purchasePolicy(request, "test@gmail.com");
    }

    @Test
    @DisplayName("Should purchase policy successfully (Age >= 55)")
    void purchasePolicy_WithAgeOver55() {
        PurchasePolicyRequest request = new PurchasePolicyRequest(1L, null, 60, null);
        
        when(productRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(product));
        when(purchasedPolicyRepository.existsByCustomerEmailAndPolicyTypeAndStatus(anyString(), any(), any())).thenReturn(false);
        when(purchasedPolicyRepository.save(any(PurchasedPolicy.class))).thenReturn(purchasedPolicy);

        purchasedPolicyService.purchasePolicy(request, "test@gmail.com");
    }

    @Test
    @DisplayName("Should throw if policy product is not active or not found")
    void purchasePolicy_WithInvalidProduct_ShouldThrow() {
        PurchasePolicyRequest request = new PurchasePolicyRequest(1L, null, 30, null);
        when(productRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchasedPolicyService.purchasePolicy(request, "test@gmail.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should throw if customer already has active policy of same type")
    void purchasePolicy_WithExistingActivePolicy_ShouldThrow() {
        PurchasePolicyRequest request = new PurchasePolicyRequest(1L, null, 30, null);
        when(productRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(product));
        when(purchasedPolicyRepository.existsByCustomerEmailAndPolicyTypeAndStatus(
                "test@gmail.com", PolicyType.HEALTH, PolicyStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> purchasedPolicyService.purchasePolicy(request, "test@gmail.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should silently catch exception during RabbitMQ message publishing on purchase")
    void purchasePolicy_RabbitMQError_ShouldCatchException() {
        PurchasePolicyRequest request = new PurchasePolicyRequest(1L, null, 30, null);
        when(productRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(product));
        when(purchasedPolicyRepository.existsByCustomerEmailAndPolicyTypeAndStatus(anyString(), any(), any())).thenReturn(false);
        when(purchasedPolicyRepository.save(any(PurchasedPolicy.class))).thenReturn(purchasedPolicy);
        
        doThrow(new RuntimeException("Rabbit error")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        PurchasedPolicyResponse response = purchasedPolicyService.purchasePolicy(request, "test@gmail.com");
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Should fetch all policies for customer")
    void getMyPolicies_ShouldReturnPolicies() {
        when(purchasedPolicyRepository.findByCustomerEmail("test@gmail.com")).thenReturn(List.of(purchasedPolicy));

        List<PurchasedPolicyResponse> responses = purchasedPolicyService.getMyPolicies("test@gmail.com");

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("Should fetch policy by ID for admin")
    void getPolicyById_ForAdmin_ShouldReturnPolicy() {
        when(purchasedPolicyRepository.findById(100L)).thenReturn(Optional.of(purchasedPolicy));

        PurchasedPolicyResponse response = purchasedPolicyService.getPolicyById(100L, "admin@gmail.com", "ADMIN");

        assertThat(response.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Should throw when policy by ID for admin not found")
    void getPolicyById_ForAdmin_NotFound_ShouldThrow() {
        when(purchasedPolicyRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchasedPolicyService.getPolicyById(100L, "admin@gmail.com", "ADMIN"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should fetch policy by ID for owner")
    void getPolicyById_ForUserOwner_ShouldReturnPolicy() {
        when(purchasedPolicyRepository.findByIdAndCustomerEmail(100L, "test@gmail.com")).thenReturn(Optional.of(purchasedPolicy));

        PurchasedPolicyResponse response = purchasedPolicyService.getPolicyById(100L, "test@gmail.com", "USER");

        assertThat(response.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Should throw if user tries to fetch someone else's policy")
    void getPolicyById_ForOtherUser_ShouldThrow() {
        when(purchasedPolicyRepository.findByIdAndCustomerEmail(100L, "other@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchasedPolicyService.getPolicyById(100L, "other@gmail.com", "USER"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should cancel policy successfully")
    void cancelPolicy_WithActivePolicy_ShouldCancel() {
        when(purchasedPolicyRepository.findByIdAndCustomerEmail(100L, "test@gmail.com")).thenReturn(Optional.of(purchasedPolicy));
        when(purchasedPolicyRepository.save(any(PurchasedPolicy.class))).thenAnswer(i -> i.getArguments()[0]);

        PurchasedPolicyResponse response = purchasedPolicyService.cancelPolicy(100L, "test@gmail.com");

        assertThat(response.getStatus()).isEqualTo(PolicyStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should throw when trying to cancel cancelled policy")
    void cancelPolicy_AlreadyCancelled_ShouldThrow() {
        purchasedPolicy.setStatus(PolicyStatus.CANCELLED);
        when(purchasedPolicyRepository.findByIdAndCustomerEmail(100L, "test@gmail.com")).thenReturn(Optional.of(purchasedPolicy));

        assertThatThrownBy(() -> purchasedPolicyService.cancelPolicy(100L, "test@gmail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already cancelled");
    }

    @Test
    @DisplayName("Should throw when trying to cancel expired policy")
    void cancelPolicy_Expired_ShouldThrow() {
        purchasedPolicy.setStatus(PolicyStatus.EXPIRED);
        when(purchasedPolicyRepository.findByIdAndCustomerEmail(100L, "test@gmail.com")).thenReturn(Optional.of(purchasedPolicy));

        assertThatThrownBy(() -> purchasedPolicyService.cancelPolicy(100L, "test@gmail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot cancel an expired policy");
    }

    @Test
    @DisplayName("Should auto expire policies")
    void autoExpirePolicies_ShouldExpirePolicies() {
        purchasedPolicy.setEndDate(LocalDate.now().minusDays(1));
        when(purchasedPolicyRepository.findExpiredPolicies(any(LocalDate.class))).thenReturn(List.of(purchasedPolicy));

        purchasedPolicyService.autoExpirePolicies();

        assertThat(purchasedPolicy.getStatus()).isEqualTo(PolicyStatus.EXPIRED);
        verify(purchasedPolicyRepository).saveAll(any());
    }

    @Test
    @DisplayName("Should skip auto expire if no policies found")
    void autoExpirePolicies_Empty_ShouldDoNothing() {
        when(purchasedPolicyRepository.findExpiredPolicies(any(LocalDate.class))).thenReturn(List.of());

        purchasedPolicyService.autoExpirePolicies();

        verify(purchasedPolicyRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should notify expiring policies")
    void notifyExpiringPolicies_ShouldSendMessages() {
        purchasedPolicy.setEndDate(LocalDate.now().plusDays(10));
        when(purchasedPolicyRepository.findPoliciesExpiringSoon(any(), any())).thenReturn(List.of(purchasedPolicy));

        purchasedPolicyService.notifyExpiringPolicies();

        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("Should skip notify expiring if no policies found")
    void notifyExpiringPolicies_Empty_ShouldDoNothing() {
        when(purchasedPolicyRepository.findPoliciesExpiringSoon(any(), any())).thenReturn(List.of());

        purchasedPolicyService.notifyExpiringPolicies();

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("Should silently catch exception during RabbitMQ message publishing on expiring notify")
    void notifyExpiringPolicies_RabbitMQError_ShouldCatchException() {
        purchasedPolicy.setEndDate(LocalDate.now().plusDays(10));
        when(purchasedPolicyRepository.findPoliciesExpiringSoon(any(), any())).thenReturn(List.of(purchasedPolicy));
        doThrow(new RuntimeException("Rabbit error")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        purchasedPolicyService.notifyExpiringPolicies();
    }
    
    @Test
    @DisplayName("Should validate policy successfully")
    void validatePolicy_ShouldReturnTrue() {
        when(purchasedPolicyRepository.findByPolicyNumber("POL-2026-000001")).thenReturn(Optional.of(purchasedPolicy));
        PolicyValidationResponse response = purchasedPolicyService.validatePolicy("POL-2026-000001", "test@gmail.com");
        assertThat(response.isValid()).isTrue();
    }

    @Test
    @DisplayName("Should throw validate policy not found")
    void validatePolicy_NotFound_ShouldThrow() {
        when(purchasedPolicyRepository.findByPolicyNumber("POL-2026-000001")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> purchasedPolicyService.validatePolicy("POL-2026-000001", "test@gmail.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should throw validate policy wrong user")
    void validatePolicy_WrongUser_ShouldThrow() {
        when(purchasedPolicyRepository.findByPolicyNumber("POL-2026-000001")).thenReturn(Optional.of(purchasedPolicy));
        assertThatThrownBy(() -> purchasedPolicyService.validatePolicy("POL-2026-000001", "other@gmail.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should throw validate policy not active")
    void validatePolicy_NotActive_ShouldThrow() {
        purchasedPolicy.setStatus(PolicyStatus.CANCELLED);
        when(purchasedPolicyRepository.findByPolicyNumber("POL-2026-000001")).thenReturn(Optional.of(purchasedPolicy));
        assertThatThrownBy(() -> purchasedPolicyService.validatePolicy("POL-2026-000001", "test@gmail.com"))
                .isInstanceOf(RuntimeException.class);
    }
    
    @Test
    @DisplayName("Should fetch all policies")
    void getAllPolicies_ShouldReturnAll() {
        when(purchasedPolicyRepository.findAll()).thenReturn(List.of(purchasedPolicy));
        List<PurchasedPolicyResponse> responses = purchasedPolicyService.getAllPolicies();
        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("Map to policy response with cancelled policy gives 0 days remaining")
    void mapToPolicyResponse_Cancelled_ShouldGiveZeroDays() {
        purchasedPolicy.setStatus(PolicyStatus.CANCELLED);
        when(purchasedPolicyRepository.findAll()).thenReturn(List.of(purchasedPolicy));
        List<PurchasedPolicyResponse> responses = purchasedPolicyService.getAllPolicies();
        assertThat(responses.get(0).getDaysRemaining()).isEqualTo(0);
    }
}
