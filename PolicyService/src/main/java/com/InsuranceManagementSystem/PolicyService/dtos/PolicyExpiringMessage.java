package com.InsuranceManagementSystem.PolicyService.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyExpiringMessage {
    private Long policyId;
    private String policyNumber;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String policyName;
    private LocalDate expiryDate;
    private Integer daysRemaining;
}
