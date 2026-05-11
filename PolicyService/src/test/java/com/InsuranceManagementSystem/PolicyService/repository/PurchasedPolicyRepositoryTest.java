package com.InsuranceManagementSystem.PolicyService.repository;

import com.InsuranceManagementSystem.PolicyService.entity.PolicyStatus;
import com.InsuranceManagementSystem.PolicyService.entity.PolicyType;
import com.InsuranceManagementSystem.PolicyService.entity.PurchasedPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PurchasedPolicyRepositoryTest {

    @Autowired
    private PurchasedPolicyRepository repository;

    private PurchasedPolicy policy;

    @BeforeEach
    void setUp() {
        policy = PurchasedPolicy.builder()
                .policyNumber("POL-123")
                .customerEmail("test@test.com")
                .productId(1L)
                .productName("Test Health")
                .policyType(PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("100000"))
                .premiumPaid(new BigDecimal("1000"))
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(10))
                .status(PolicyStatus.ACTIVE)
                .build();
        repository.save(policy);
    }

    @Test
    @DisplayName("Should find by customer email")
    void findByCustomerEmail() {
        List<PurchasedPolicy> policies = repository.findByCustomerEmail("test@test.com");
        assertThat(policies).hasSize(1);
    }

    @Test
    @DisplayName("Should find by policy number")
    void findByPolicyNumber() {
        Optional<PurchasedPolicy> result = repository.findByPolicyNumber("POL-123");
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("Should check existence by email, type, status")
    void existsByCustomerEmailAndPolicyTypeAndStatus() {
        boolean exists = repository.existsByCustomerEmailAndPolicyTypeAndStatus(
                "test@test.com", PolicyType.HEALTH, PolicyStatus.ACTIVE);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should find expired policies")
    void findExpiredPolicies() {
        PurchasedPolicy expired = PurchasedPolicy.builder()
                .policyNumber("POL-OLD")
                .customerEmail("test2@test.com")
                .productId(1L)
                .productName("Test Health")
                .policyType(PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("100000"))
                .premiumPaid(new BigDecimal("1000"))
                .startDate(LocalDate.now().minusDays(365))
                .endDate(LocalDate.now().minusDays(1))
                .status(PolicyStatus.ACTIVE)
                .build();
        repository.save(expired);

        List<PurchasedPolicy> policies = repository.findExpiredPolicies(LocalDate.now());
        assertThat(policies).hasSize(1);
        assertThat(policies.get(0).getPolicyNumber()).isEqualTo("POL-OLD");
    }

    @Test
    @DisplayName("Should find policies expiring soon")
    void findPoliciesExpiringSoon() {
        List<PurchasedPolicy> policies = repository.findPoliciesExpiringSoon(LocalDate.now(), LocalDate.now().plusDays(30));
        assertThat(policies).hasSize(1);
        assertThat(policies.get(0).getPolicyNumber()).isEqualTo("POL-123");
    }
}
