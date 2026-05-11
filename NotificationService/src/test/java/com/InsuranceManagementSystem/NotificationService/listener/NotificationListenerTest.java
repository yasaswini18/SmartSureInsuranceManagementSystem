package com.InsuranceManagementSystem.NotificationService.listener;

import com.InsuranceManagementSystem.NotificationService.dtos.MessageDTOs.*;
import com.InsuranceManagementSystem.NotificationService.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationListener notificationListener;

    @Test
    @DisplayName("Should send welcome email when email is present")
    void handleUserRegistered_ShouldSendEmail() {
        UserRegisteredMessage msg = new UserRegisteredMessage();
        msg.setEmail("test@user.com");
        msg.setFirstName("Test");

        notificationListener.handleUserRegistered(msg);

        verify(emailService, times(1)).sendWelcomeEmail(msg);
    }

    @Test
    @DisplayName("Should skip welcome email when email is null or empty")
    void handleUserRegistered_WhenEmailMissing_ShouldSkip() {
        UserRegisteredMessage msg1 = new UserRegisteredMessage();
        msg1.setEmail(null);

        UserRegisteredMessage msg2 = new UserRegisteredMessage();
        msg2.setEmail("  ");

        notificationListener.handleUserRegistered(msg1);
        notificationListener.handleUserRegistered(msg2);

        verify(emailService, never()).sendWelcomeEmail(any());
    }

    @Test
    @DisplayName("Should catch exception when sending welcome email fails")
    void handleUserRegistered_WhenException_ShouldCatch() {
        UserRegisteredMessage msg = new UserRegisteredMessage();
        msg.setEmail("test@user.com");
        
        doThrow(new RuntimeException("Mail server down")).when(emailService).sendWelcomeEmail(any());

        notificationListener.handleUserRegistered(msg);

        verify(emailService, times(1)).sendWelcomeEmail(msg);
    }

    @Test
    @DisplayName("Should send policy purchased email when email is present")
    void handlePolicyPurchased_ShouldSendEmail() {
        PolicyPurchasedMessage msg = new PolicyPurchasedMessage();
        msg.setCustomerEmail("test@user.com");

        notificationListener.handlePolicyPurchased(msg);

        verify(emailService, times(1)).sendPolicyPurchasedEmail(msg);
    }

    @Test
    @DisplayName("Should skip policy purchased email when email is missing")
    void handlePolicyPurchased_WhenEmailMissing_ShouldSkip() {
        PolicyPurchasedMessage msg = new PolicyPurchasedMessage();
        msg.setCustomerEmail("");

        notificationListener.handlePolicyPurchased(msg);

        verify(emailService, never()).sendPolicyPurchasedEmail(any());
    }
    
    @Test
    @DisplayName("Should catch exception when sending policy purchased email fails")
    void handlePolicyPurchased_WhenException_ShouldCatch() {
        PolicyPurchasedMessage msg = new PolicyPurchasedMessage();
        msg.setCustomerEmail("test@user.com");
        
        doThrow(new RuntimeException("Mail down")).when(emailService).sendPolicyPurchasedEmail(any());

        notificationListener.handlePolicyPurchased(msg);

        verify(emailService, times(1)).sendPolicyPurchasedEmail(msg);
    }

    @Test
    @DisplayName("Should send policy expiring email when days is 30, 15, or 7")
    void handlePolicyExpiring_ShouldSendEmailOnSpecificDays() {
        PolicyExpiringMessage msg30 = new PolicyExpiringMessage();
        msg30.setCustomerEmail("test@user.com");
        msg30.setDaysRemaining(30);

        PolicyExpiringMessage msg15 = new PolicyExpiringMessage();
        msg15.setCustomerEmail("test@user.com");
        msg15.setDaysRemaining(15);

        PolicyExpiringMessage msg7 = new PolicyExpiringMessage();
        msg7.setCustomerEmail("test@user.com");
        msg7.setDaysRemaining(7);

        notificationListener.handlePolicyExpiring(msg30);
        notificationListener.handlePolicyExpiring(msg15);
        notificationListener.handlePolicyExpiring(msg7);

        verify(emailService, times(3)).sendPolicyExpiringEmail(any());
    }

    @Test
    @DisplayName("Should skip policy expiring email when days is not 30, 15, or 7")
    void handlePolicyExpiring_ShouldSkipOtherDays() {
        PolicyExpiringMessage msg10 = new PolicyExpiringMessage();
        msg10.setCustomerEmail("test@user.com");
        msg10.setDaysRemaining(10);

        PolicyExpiringMessage msgnull = new PolicyExpiringMessage();
        msgnull.setCustomerEmail("test@user.com");
        msgnull.setDaysRemaining(null);

        notificationListener.handlePolicyExpiring(msg10);
        notificationListener.handlePolicyExpiring(msgnull);

        verify(emailService, never()).sendPolicyExpiringEmail(any());
    }

    @Test
    @DisplayName("Should skip policy expiring email when email is missing")
    void handlePolicyExpiring_WhenEmailMissing_ShouldSkip() {
        PolicyExpiringMessage msg = new PolicyExpiringMessage();
        msg.setDaysRemaining(30);

        notificationListener.handlePolicyExpiring(msg);

        verify(emailService, never()).sendPolicyExpiringEmail(any());
    }

    @Test
    @DisplayName("Should catch exception when sending policy expiring email fails")
    void handlePolicyExpiring_WhenException_ShouldCatch() {
        PolicyExpiringMessage msg = new PolicyExpiringMessage();
        msg.setCustomerEmail("test@user.com");
        msg.setDaysRemaining(7);
        
        doThrow(new RuntimeException("Error")).when(emailService).sendPolicyExpiringEmail(any());

        notificationListener.handlePolicyExpiring(msg);

        verify(emailService, times(1)).sendPolicyExpiringEmail(msg);
    }

    @Test
    @DisplayName("Should send claim submitted emails to customer and admin")
    void handleClaimSubmitted_ShouldSendEmails() {
        ClaimSubmittedMessage msg = new ClaimSubmittedMessage();
        msg.setCustomerEmail("test@user.com");
        msg.setClaimNumber("CLM-123");
        msg.setClaimedAmount(1000.0);

        notificationListener.handleClaimSubmitted(msg);

        verify(emailService, times(1)).sendClaimSubmittedEmail(msg);
        verify(emailService, times(1)).sendEmail(eq("yasaswini18102004@gmail.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("Should skip claim submitted email when email is missing")
    void handleClaimSubmitted_WhenEmailMissing_ShouldSkip() {
        ClaimSubmittedMessage msg = new ClaimSubmittedMessage();
        msg.setCustomerEmail(null);

        notificationListener.handleClaimSubmitted(msg);

        verify(emailService, never()).sendClaimSubmittedEmail(any());
    }
    
    @Test
    @DisplayName("Should catch exception when sending claim submitted email fails")
    void handleClaimSubmitted_WhenException_ShouldCatch() {
        ClaimSubmittedMessage msg = new ClaimSubmittedMessage();
        msg.setCustomerEmail("test@user.com");
        
        doThrow(new RuntimeException("Error")).when(emailService).sendClaimSubmittedEmail(any());

        notificationListener.handleClaimSubmitted(msg);

        verify(emailService, times(1)).sendClaimSubmittedEmail(msg);
    }

    @Test
    @DisplayName("Should send claim reviewed email (Approved)")
    void handleClaimReviewed_Approved_ShouldSendEmail() {
        ClaimReviewedMessage msg = new ClaimReviewedMessage();
        msg.setCustomerEmail("test@user.com");
        msg.setDecision("APPROVED");

        notificationListener.handleClaimReviewed(msg);

        verify(emailService, times(1)).sendClaimReviewedEmail(msg);
    }

    @Test
    @DisplayName("Should send claim reviewed email (Rejected)")
    void handleClaimReviewed_Rejected_ShouldSendEmail() {
        ClaimReviewedMessage msg = new ClaimReviewedMessage();
        msg.setCustomerEmail("test@user.com");
        msg.setDecision("REJECTED");

        notificationListener.handleClaimReviewed(msg);

        verify(emailService, times(1)).sendClaimReviewedEmail(msg);
    }

    @Test
    @DisplayName("Should skip claim reviewed email when email is missing")
    void handleClaimReviewed_WhenEmailMissing_ShouldSkip() {
        ClaimReviewedMessage msg = new ClaimReviewedMessage();
        msg.setCustomerEmail(" ");

        notificationListener.handleClaimReviewed(msg);

        verify(emailService, never()).sendClaimReviewedEmail(any());
    }
    
    @Test
    @DisplayName("Should catch exception when sending claim reviewed email fails")
    void handleClaimReviewed_WhenException_ShouldCatch() {
        ClaimReviewedMessage msg = new ClaimReviewedMessage();
        msg.setCustomerEmail("test@user.com");
        msg.setDecision("APPROVED");
        
        doThrow(new RuntimeException("Error")).when(emailService).sendClaimReviewedEmail(any());

        notificationListener.handleClaimReviewed(msg);

        verify(emailService, times(1)).sendClaimReviewedEmail(msg);
    }
}
