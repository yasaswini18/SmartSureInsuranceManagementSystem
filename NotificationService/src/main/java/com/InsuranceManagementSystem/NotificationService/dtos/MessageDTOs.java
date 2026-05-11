package com.InsuranceManagementSystem.NotificationService.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MessageDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRegisteredMessage {
        private Long userId;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private LocalDateTime registeredAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PolicyPurchasedMessage {
        private Long policyId;
        private String policyNumber;
        private Long customerId;
        private String customerName;
        private String customerEmail;
        private String policyName;
        private String policyType;
        private Double basePremium;
        private Double coverageAmount;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDateTime purchasedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PolicyExpiringMessage {
        private Long policyId;
        private String policyNumber;
        private Long customerId;
        private String customerName;
        private String customerEmail;
        private String policyName;
        private LocalDate endDate;
        private Integer daysRemaining;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClaimSubmittedMessage {
        private Long claimId;
        private String claimNumber;
        private Long customerId;
        private String customerName;
        private String customerEmail;
        private Long policyId;
        private String policyNumber;
        private Double claimedAmount;
        private String claimType;
        private LocalDate incidentDate;
        private LocalDateTime submittedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClaimReviewedMessage {
        private Long claimId;
        private String claimNumber;
        private Long customerId;
        private String customerName;
        private String customerEmail;
        private String policyNumber;
        private Double claimedAmount;
        private Double approvedAmount;
        private String decision;
        private String adminRemarks;
        private LocalDateTime reviewedAt;
    }
}
