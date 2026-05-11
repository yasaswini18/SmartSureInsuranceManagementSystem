package com.InsuranceManagementSystem.PolicyService.service;

import com.InsuranceManagementSystem.PolicyService.dto.PolicyValidationResponse;
import com.InsuranceManagementSystem.PolicyService.dto.PurchasePolicyRequest;
import com.InsuranceManagementSystem.PolicyService.dto.PurchasedPolicyResponse;
import com.InsuranceManagementSystem.PolicyService.entity.PolicyProduct;
import com.InsuranceManagementSystem.PolicyService.entity.PurchasedPolicy;
import com.InsuranceManagementSystem.PolicyService.entity.PolicyStatus;
import com.InsuranceManagementSystem.PolicyService.repository.PolicyProductRepository;
import com.InsuranceManagementSystem.PolicyService.repository.PurchasedPolicyRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing purchased policies.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PurchasedPolicyService {

    private final PurchasedPolicyRepository purchasedPolicyRepository;
    private final PolicyProductRepository productRepository;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    @Value("${policy.number.prefix}")
    private String policyNumberPrefix;

    /**
     * Purchases a new policy for a customer.
     *
     * @param request the details of the policy purchase request
     * @param customerEmail the email of the purchasing customer
     * @return the purchased policy as a response object
     */
    @Transactional
    public PurchasedPolicyResponse purchasePolicy(
            PurchasePolicyRequest request,
            String customerEmail
    ) {
        log.info("Customer {} purchasing policy for product id: {}",
                customerEmail, request.getProductId());

        PolicyProduct product = productRepository
                .findByIdAndIsActiveTrue(request.getProductId())
                .orElseThrow(() -> new RuntimeException(
                    "Policy product not found or no longer available: "
                    + request.getProductId()
                ));

        boolean alreadyHasActivePolicy =
                purchasedPolicyRepository
                .existsByCustomerEmailAndPolicyTypeAndStatus(
                        customerEmail,
                        product.getType(),
                        PolicyStatus.ACTIVE
                );

        if (alreadyHasActivePolicy) {
            throw new RuntimeException(
                "You already have an active " + product.getType().name()
                + " policy. Please cancel it before purchasing a new one."
            );
        }

        BigDecimal calculatedPremium = calculatePremium(
                product.getBasePremium(),
                request.getAge(),
                product.getCoverageAmount()
        );

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(product.getDurationMonths());

        String policyNumber = generatePolicyNumber();

        PurchasedPolicy purchasedPolicy = PurchasedPolicy.builder()
                .policyNumber(policyNumber)
                .customerEmail(customerEmail)
                .productId(product.getId())
                .productName(product.getName())
                .policyType(product.getType())
                .coverageAmount(product.getCoverageAmount())
                .premiumPaid(calculatedPremium)
                .startDate(startDate)
                .endDate(endDate)
                .status(PolicyStatus.ACTIVE)
                .build();

        PurchasedPolicy saved = purchasedPolicyRepository.save(purchasedPolicy);
        log.info("Policy purchased successfully: {}", saved.getPolicyNumber());

        try {
            com.InsuranceManagementSystem.PolicyService.dtos.PolicyPurchasedMessage message = new com.InsuranceManagementSystem.PolicyService.dtos.PolicyPurchasedMessage(
                    saved.getId(),
                    saved.getPolicyNumber(),
                    0L,
                    customerEmail,
                    customerEmail,
                    saved.getProductName(),
                    saved.getPolicyType().name(),
                    product.getBasePremium().doubleValue(),
                    saved.getCoverageAmount().doubleValue(),
                    saved.getStartDate(),
                    saved.getEndDate(),
                    saved.getCreatedAt()
            );
            rabbitTemplate.convertAndSend(
                    com.InsuranceManagementSystem.PolicyService.config.RabbitMQConfig.EXCHANGE, 
                    com.InsuranceManagementSystem.PolicyService.config.RabbitMQConfig.POLICY_PURCHASED_QUEUE, 
                    message
            );
            log.info("Published PolicyPurchasedMessage for policy: {}", saved.getPolicyNumber());
        } catch (Exception e) {
            log.error("Failed to publish PolicyPurchasedMessage for policy: {}", saved.getPolicyNumber(), e);
        }

        return mapToPolicyResponse(saved);
    }

    /**
     * Calculates the premium based on base premium, age, and coverage amount.
     *
     * @param basePremium the base premium of the product
     * @param age the age of the customer
     * @param coverageAmount the coverage amount
     * @return the calculated premium
     */
    private BigDecimal calculatePremium(
            BigDecimal basePremium,
            int age,
            BigDecimal coverageAmount
    ) {
        BigDecimal ageFactor;
        if (age < 25) {
            ageFactor = new BigDecimal("1.1");
        } else if (age < 35) {
            ageFactor = new BigDecimal("1.0");
        } else if (age < 45) {
            ageFactor = new BigDecimal("1.2");
        } else if (age < 55) {
            ageFactor = new BigDecimal("1.4");
        } else {
            ageFactor = new BigDecimal("1.7");
        }

        BigDecimal coverageFactor;
        if (coverageAmount.compareTo(new BigDecimal("100000")) <= 0) {
            coverageFactor = new BigDecimal("1.0");
        } else if (coverageAmount.compareTo(new BigDecimal("500000")) <= 0) {
            coverageFactor = new BigDecimal("1.15");
        } else {
            coverageFactor = new BigDecimal("1.3");
        }

        BigDecimal calculatedPremium = basePremium
                .multiply(ageFactor)
                .multiply(coverageFactor)
                .setScale(2, RoundingMode.HALF_UP);

        log.info("Premium calculated: base={}, age={}, ageFactor={}, coverageFactor={}, final={}",
                basePremium, age, ageFactor, coverageFactor, calculatedPremium);

        return calculatedPremium;
    }

    /**
     * Generates a unique policy number.
     *
     * @return the generated policy number
     */
    private String generatePolicyNumber() {
        int year = LocalDate.now().getYear();
        long totalPolicies = purchasedPolicyRepository.count();

        return String.format("%s-%d-%06d",
                policyNumberPrefix, year, totalPolicies + 1);
    }

    /**
     * Retrieves all policies purchased by a specific customer.
     *
     * @param customerEmail the email of the customer
     * @return a list of purchased policies
     */
    public List<PurchasedPolicyResponse> getMyPolicies(String customerEmail) {
        log.info("Fetching policies for customer: {}", customerEmail);

        return purchasedPolicyRepository
                .findByCustomerEmail(customerEmail)
                .stream()
                .map(this::mapToPolicyResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a purchased policy by its ID.
     *
     * @param policyId the ID of the policy
     * @param customerEmail the email of the requesting user
     * @param role the role of the requesting user
     * @return the requested policy as a response object
     */
    public PurchasedPolicyResponse getPolicyById(
            Long policyId,
            String customerEmail,
            String role
    ) {
        log.info("Fetching policy id: {} for user: {}", policyId, customerEmail);

        if (role.equals("ADMIN")) {
            PurchasedPolicy policy = purchasedPolicyRepository
                    .findById(policyId)
                    .orElseThrow(() -> new RuntimeException(
                        "Policy not found with id: " + policyId
                    ));
            return mapToPolicyResponse(policy);
        } else {
            PurchasedPolicy policy = purchasedPolicyRepository
                    .findByIdAndCustomerEmail(policyId, customerEmail)
                    .orElseThrow(() -> new RuntimeException(
                        "Policy not found or you don't have permission to view it"
                    ));
            return mapToPolicyResponse(policy);
        }
    }

    /**
     * Retrieves all purchased policies in the system.
     *
     * @return a list of all purchased policies
     */
    public List<PurchasedPolicyResponse> getAllPolicies() {
        log.info("Admin fetching all purchased policies");

        return purchasedPolicyRepository
                .findAll()
                .stream()
                .map(this::mapToPolicyResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cancels an active purchased policy.
     *
     * @param policyId the ID of the policy to cancel
     * @param customerEmail the email of the customer cancelling the policy
     * @return the cancelled policy as a response object
     */
    @Transactional
    public PurchasedPolicyResponse cancelPolicy(
            Long policyId,
            String customerEmail
    ) {
        log.info("Customer {} cancelling policy id: {}", customerEmail, policyId);

        PurchasedPolicy policy = purchasedPolicyRepository
                .findByIdAndCustomerEmail(policyId, customerEmail)
                .orElseThrow(() -> new RuntimeException(
                    "Policy not found or you don't have permission to cancel it"
                ));

        if (policy.getStatus() == PolicyStatus.CANCELLED) {
            throw new RuntimeException("Policy is already cancelled");
        }

        if (policy.getStatus() == PolicyStatus.EXPIRED) {
            throw new RuntimeException("Cannot cancel an expired policy");
        }

        policy.setStatus(PolicyStatus.CANCELLED);
        PurchasedPolicy cancelled = purchasedPolicyRepository.save(policy);

        log.info("Policy cancelled: {}", cancelled.getPolicyNumber());

        return mapToPolicyResponse(cancelled);
    }

    /**
     * Scheduled task to automatically expire policies whose end date has passed.
     * Runs daily at midnight.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void autoExpirePolicies() {
        log.info("Running auto-expiry job at: {}", LocalDateTime.now());

        List<PurchasedPolicy> expiredPolicies =
                purchasedPolicyRepository.findExpiredPolicies(LocalDate.now());

        if (expiredPolicies.isEmpty()) {
            log.info("No policies to expire today");
            return;
        }

        expiredPolicies.forEach(policy -> {
            policy.setStatus(PolicyStatus.EXPIRED);
            log.info("Auto-expiring policy: {}", policy.getPolicyNumber());
        });

        purchasedPolicyRepository.saveAll(expiredPolicies);

        log.info("Auto-expiry job completed. {} policies expired",
                expiredPolicies.size());
    }

    /**
     * Scheduled task to notify users about policies expiring within 30 days.
     * Runs daily at 9:00 AM.
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void notifyExpiringPolicies() {
        log.info("Running notifyExpiringPolicies job at: {}", LocalDateTime.now());
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        
        List<PurchasedPolicy> expiringPolicies = purchasedPolicyRepository.findPoliciesExpiringSoon(today, thirtyDaysFromNow);

        if (expiringPolicies.isEmpty()) {
            log.info("No policies expiring soon");
            return;
        }

        expiringPolicies.forEach(policy -> {
            try {
                com.InsuranceManagementSystem.PolicyService.dtos.PolicyExpiringMessage message = new com.InsuranceManagementSystem.PolicyService.dtos.PolicyExpiringMessage(
                        policy.getId(),
                        policy.getPolicyNumber(),
                        0L,
                        policy.getCustomerEmail(),
                        policy.getCustomerEmail(),
                        policy.getProductName(),
                        policy.getEndDate(),
                        (int) ChronoUnit.DAYS.between(today, policy.getEndDate())
                );
                rabbitTemplate.convertAndSend(
                        com.InsuranceManagementSystem.PolicyService.config.RabbitMQConfig.EXCHANGE, 
                        com.InsuranceManagementSystem.PolicyService.config.RabbitMQConfig.POLICY_EXPIRING_QUEUE, 
                        message
                );
                log.info("Published PolicyExpiringMessage for policy: {}", policy.getPolicyNumber());
            } catch(Exception e) {
                log.error("Failed to publish PolicyExpiringMessage for policy: {}", policy.getPolicyNumber(), e);
            }
        });
    }

    /**
     * Maps a PurchasedPolicy entity to a PurchasedPolicyResponse DTO.
     *
     * @param policy the PurchasedPolicy entity
     * @return the mapped PurchasedPolicyResponse DTO
     */
    private PurchasedPolicyResponse mapToPolicyResponse(PurchasedPolicy policy) {

        long daysRemaining = 0;
        if (policy.getStatus() == PolicyStatus.ACTIVE) {
            daysRemaining = ChronoUnit.DAYS.between(
                    LocalDate.now(),
                    policy.getEndDate()
            );
            daysRemaining = Math.max(0, daysRemaining);
        }

        return PurchasedPolicyResponse.builder()
                .id(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .customerEmail(policy.getCustomerEmail())
                .productId(policy.getProductId())
                .productName(policy.getProductName())
                .policyType(policy.getPolicyType())
                .coverageAmount(policy.getCoverageAmount())
                .premiumPaid(policy.getPremiumPaid())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .status(policy.getStatus())
                .daysRemaining(daysRemaining)
                .createdAt(policy.getCreatedAt())
                .build();
    }

    /**
     * Validates a purchased policy by checking if it belongs to the specified user and is active.
     *
     * @param policyNumber the unique policy number
     * @param customerEmail the email of the requesting customer
     * @return a validation response with details of the policy
     */
    public PolicyValidationResponse validatePolicy(
            String policyNumber,
            String customerEmail
    ) {

        PurchasedPolicy policy = purchasedPolicyRepository
                .findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new RuntimeException("Policy not found"));

        if (!policy.getCustomerEmail().equals(customerEmail)) {
            throw new RuntimeException("Policy does not belong to this user");
        }

        if (!policy.getStatus().name().equals("ACTIVE")) {
            throw new RuntimeException("Policy is not active");
        }

        return new PolicyValidationResponse(
                true,
                policy.getPolicyNumber(),
                policy.getCustomerEmail(),
                policy.getProductName(),
                policy.getPolicyType().name(),
                policy.getCoverageAmount(),
                policy.getStartDate(),
                policy.getStatus().name(),
                "Policy is valid"
        );
    }
}