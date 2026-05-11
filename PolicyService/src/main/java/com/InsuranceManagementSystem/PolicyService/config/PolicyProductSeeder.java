package com.InsuranceManagementSystem.PolicyService.config;

import com.InsuranceManagementSystem.PolicyService.entity.PolicyProduct;
import com.InsuranceManagementSystem.PolicyService.entity.PolicyType;
import com.InsuranceManagementSystem.PolicyService.repository.PolicyProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PolicyProductSeeder {

    private static final String SYSTEM_USER = "SYSTEM";

    @Bean
    CommandLineRunner seedPolicyProducts(PolicyProductRepository repository) {
        return args -> {
            List<PolicyProduct> seedProducts = List.of(
                product("Basic Health Cover", "Covers hospitalization expenses for individuals including room rent, surgeon fees and ICU charges for short term treatments.", PolicyType.HEALTH, 299, 100000, 12, 18, 65),
                product("Family Health Shield", "Comprehensive health coverage for your entire family including spouse, children and parents covering major surgeries and critical illness.", PolicyType.HEALTH, 799, 500000, 12, 18, 65),
                product("Senior Citizen Health Plan", "Specially designed health insurance for citizens above 60 years covering pre-existing conditions, dialysis, chemotherapy and regular hospitalization.", PolicyType.HEALTH, 1299, 1000000, 12, 60, 80),
                product("Term Life Basic", "Pure life insurance plan that pays a lump sum amount to your nominee in case of your unfortunate death during the policy period.", PolicyType.LIFE, 199, 1000000, 12, 18, 55),
                product("Term Life Plus", "Enhanced life cover with accidental death benefit. Nominee receives double the coverage amount in case of accidental death.", PolicyType.LIFE, 499, 5000000, 12, 18, 55),
                product("Whole Life Secure", "Lifetime life insurance coverage with savings benefit. Provides financial security to your family along with a maturity bonus after the policy term.", PolicyType.LIFE, 999, 10000000, 12, 18, 55),
                product("Two Wheeler Third Party", "Mandatory third party bike insurance covering damage or injury caused to another person or their vehicle in an accident involving your bike.", PolicyType.VEHICLE, 149, 100000, 12, 18, 70),
                product("Comprehensive Bike Cover", "Complete bike insurance covering third party liability plus your own bike damage due to accident, theft, fire, flood or natural calamity.", PolicyType.VEHICLE, 399, 500000, 12, 18, 70),
                product("Car Comprehensive Plan", "Full coverage car insurance protecting against own damage, third party claims, theft and natural disasters with roadside assistance included.", PolicyType.VEHICLE, 899, 1500000, 12, 18, 70),
                product("Home Structure Cover", "Covers physical damage to the structure of your home caused by fire, earthquake, flood, storm or any other natural or man-made disaster.", PolicyType.PROPERTY, 349, 1000000, 12, 21, 70),
                product("Home Content Protection", "Protects the contents inside your home including furniture, electronics, jewellery and appliances against theft, fire and accidental damage.", PolicyType.PROPERTY, 249, 300000, 12, 21, 70),
                product("Complete Home Shield", "All-in-one property insurance covering both the structure of your home and its contents along with temporary accommodation cost if home becomes unlivable.", PolicyType.PROPERTY, 699, 2500000, 12, 21, 70),
                product("Domestic Travel Safe", "Travel insurance for trips within India covering trip cancellation, loss of baggage, accidental injury and emergency medical expenses during travel.", PolicyType.TRAVEL, 99, 50000, 6, 5, 70),
                product("International Travel Guard", "Comprehensive overseas travel insurance covering emergency medical treatment, trip cancellation, passport loss, flight delay and personal liability abroad.", PolicyType.TRAVEL, 499, 500000, 6, 18, 70),
                product("Student Travel Plan", "Specially designed travel insurance for students going abroad for education covering medical emergencies, study interruption and sponsor protection.", PolicyType.TRAVEL, 299, 1000000, 12, 18, 30)
            );

            seedProducts.forEach(product -> {
                if (!repository.existsByNameIgnoreCaseAndType(product.getName(), product.getType())) {
                    repository.save(product);
                    log.info("Seeded policy product: {}", product.getName());
                }
            });
        };
    }

    private PolicyProduct product(
            String name,
            String description,
            PolicyType type,
            int basePremium,
            int coverageAmount,
            int durationMonths,
            int minAge,
            int maxAge
    ) {
        return PolicyProduct.builder()
            .name(name)
            .description(description)
            .type(type)
            .basePremium(BigDecimal.valueOf(basePremium))
            .coverageAmount(BigDecimal.valueOf(coverageAmount))
            .durationMonths(durationMonths)
            .minAge(minAge)
            .maxAge(maxAge)
            .isActive(true)
            .createdBy(SYSTEM_USER)
            .build();
    }
}
