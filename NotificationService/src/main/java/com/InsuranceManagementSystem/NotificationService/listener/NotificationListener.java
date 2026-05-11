package com.InsuranceManagementSystem.NotificationService.listener;

import com.InsuranceManagementSystem.NotificationService.config.RabbitMQConfig;
import com.InsuranceManagementSystem.NotificationService.dtos.MessageDTOs.*;
import com.InsuranceManagementSystem.NotificationService.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Listener class that consumes messages from RabbitMQ queues.
 * Processes various domain events and triggers email notifications to customers and admins.
 */
@Service
@Slf4j
public class NotificationListener {

    @Autowired
    private EmailService emailService;

    /**
     * Consumes user registration events and sends a welcome email.
     * 
     * @param message The user registered message from RabbitMQ.
     */
    @RabbitListener(queues = RabbitMQConfig.USER_REGISTERED_QUEUE)
    public void handleUserRegistered(UserRegisteredMessage message) {
        log.info("🔔 [NOTIFICATION] Welcome email sent to new user: {} {} ({})", 
                message.getFirstName(), message.getLastName(), message.getEmail());
        
        if (message.getEmail() == null || message.getEmail().trim().isEmpty()) {
            log.warn("Email address missing in message, skipping email for User ID {}", message.getUserId());
            return;
        }

        try {
            emailService.sendWelcomeEmail(message);
            log.info("Email sent successfully to {}", message.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage(), e);
        }
    }

    /**
     * Consumes policy purchase events and sends a confirmation email.
     * 
     * @param message The policy purchased message from RabbitMQ.
     */
    @RabbitListener(queues = RabbitMQConfig.POLICY_PURCHASED_QUEUE)
    public void handlePolicyPurchased(PolicyPurchasedMessage message) {
        log.info("🔔 [NOTIFICATION] Policy document sent to: {}. Policy {}: {}", 
                message.getCustomerEmail(), message.getPolicyType(), message.getPolicyNumber());
        
        if (message.getCustomerEmail() == null || message.getCustomerEmail().trim().isEmpty()) {
            log.warn("Email address missing in message, skipping email for policy {}", message.getPolicyNumber());
            return;
        }

        try {
            emailService.sendPolicyPurchasedEmail(message);
            log.info("Email sent successfully to {}", message.getCustomerEmail());
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage(), e);
        }
    }

    /**
     * Consumes policy expiring events and sends reminders at specific intervals.
     * 
     * @param message The policy expiring message from RabbitMQ.
     */
    @RabbitListener(queues = RabbitMQConfig.POLICY_EXPIRING_QUEUE)
    public void handlePolicyExpiring(PolicyExpiringMessage message) {
        log.info("🔔 [NOTIFICATION] Expiry reminder sent to: {}. Policy {} expires in {} days.", 
                message.getCustomerEmail(), message.getPolicyNumber(), message.getDaysRemaining());
        
        if (message.getCustomerEmail() == null || message.getCustomerEmail().trim().isEmpty()) {
            log.warn("Email address missing in message, skipping email for policy {}", message.getPolicyNumber());
            return;
        }

        int days = message.getDaysRemaining() != null ? message.getDaysRemaining() : -1;
        if (days == 30 || days == 15 || days == 7) {
            try {
                emailService.sendPolicyExpiringEmail(message);
                log.info("Email sent successfully to {}", message.getCustomerEmail());
            } catch (Exception e) {
                log.error("Failed to send email: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Consumes claim submission events, sends an acknowledgment to the customer, 
     * and notifies the admin.
     * 
     * @param message The claim submitted message from RabbitMQ.
     */
    @RabbitListener(queues = RabbitMQConfig.CLAIM_SUBMITTED_QUEUE)
    public void handleClaimSubmitted(ClaimSubmittedMessage message) {
        log.info("🔔 [NOTIFICATION] Claim acknowledgment sent to: {}. Claim Number: {}, Amount: {}", 
                message.getCustomerEmail(), message.getClaimNumber(), message.getClaimedAmount());
        
        if (message.getCustomerEmail() == null || message.getCustomerEmail().trim().isEmpty()) {
            log.warn("Email address missing in message, skipping email for claim {}", message.getClaimNumber());
            return;
        }

        try {
            emailService.sendClaimSubmittedEmail(message);
            log.info("Email sent successfully to {}", message.getCustomerEmail());
            
            emailService.sendEmail("yasaswini18102004@gmail.com", 
                    "Admin Notification: New Claim Submitted", 
                    "A new claim has been submitted. Claim Number: " + message.getClaimNumber() + ", Amount: " + message.getClaimedAmount());
            log.info("Admin notification email sent successfully for claim {}", message.getClaimNumber());
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage(), e);
        }
    }

    /**
     * Consumes claim review events and sends the decision outcome to the customer.
     * 
     * @param message The claim reviewed message from RabbitMQ.
     */
    @RabbitListener(queues = RabbitMQConfig.CLAIM_REVIEWED_QUEUE)
    public void handleClaimReviewed(ClaimReviewedMessage message) {
        if ("APPROVED".equalsIgnoreCase(message.getDecision())) {
            log.info("🔔 [NOTIFICATION] Claim APPROVED email sent to: {}. Claim Number: {}, Approved Amount: {}", 
                    message.getCustomerEmail(), message.getClaimNumber(), message.getApprovedAmount());
        } else {
            log.info("🔔 [NOTIFICATION] Claim REJECTED email sent to: {}. Claim Number: {}, Remarks: {}", 
                    message.getCustomerEmail(), message.getClaimNumber(), message.getAdminRemarks());
        }
        
        if (message.getCustomerEmail() == null || message.getCustomerEmail().trim().isEmpty()) {
            log.warn("Email address missing in message, skipping email for claim {}", message.getClaimNumber());
            return;
        }

        try {
            emailService.sendClaimReviewedEmail(message);
            log.info("Email sent successfully to {}", message.getCustomerEmail());
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage(), e);
        }
    }
}
