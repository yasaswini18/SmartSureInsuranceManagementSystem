package com.InsuranceManagementSystem.PolicyService.repository;

import com.InsuranceManagementSystem.PolicyService.entity.PolicyProduct;
import com.InsuranceManagementSystem.PolicyService.entity.PolicyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PolicyProductRepositoryTest {

    @Autowired
    private PolicyProductRepository repository;

    private PolicyProduct product;

    @BeforeEach
    void setUp() {
        product = PolicyProduct.builder()
                .name("Test Health")
                .type(PolicyType.HEALTH)
                .description("Test description")
                .basePremium(new BigDecimal("1000"))
                .coverageAmount(new BigDecimal("100000"))
                .durationMonths(12)
                .minAge(18)
                .maxAge(60)
                .isActive(true)
                .createdBy("admin")
                .build();
        repository.save(product);
    }

    @Test
    @DisplayName("Should find active products")
    void findByIsActiveTrue() {
        List<PolicyProduct> active = repository.findByIsActiveTrue();
        assertThat(active).hasSize(1);
        assertThat(active.get(0).getName()).isEqualTo("Test Health");
    }

    @Test
    @DisplayName("Should find active products by type")
    void findByIsActiveTrueAndType() {
        List<PolicyProduct> result = repository.findByIsActiveTrueAndType(PolicyType.HEALTH);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should check existence by name ignore case")
    void existsByNameIgnoreCase() {
        boolean exists = repository.existsByNameIgnoreCase("test health");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should find active by id")
    void findByIdAndIsActiveTrue() {
        Optional<PolicyProduct> result = repository.findByIdAndIsActiveTrue(product.getId());
        assertThat(result).isPresent();
    }
}
