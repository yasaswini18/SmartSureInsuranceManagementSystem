package com.InsuranceManagementSystem.NotificationService.service;

import com.InsuranceManagementSystem.NotificationService.dtos.MessageDTOs.*;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private final String fromEmail = "noreply@smartsure.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", fromEmail);
    }

    @Test
    @DisplayName("Should send simple email successfully")
    void sendEmail_ShouldSendSuccessfully() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        emailService.sendEmail("test@user.com", "Subject", "<h1>Hello</h1>");

        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("Should skip sending if toEmail is null or empty")
    void sendEmail_WhenEmailInvalid_ShouldSkip() {
        emailService.sendEmail(null, "Subject", "Body");
        emailService.sendEmail("   ", "Subject", "Body");

        verify(mailSender, never()).createMimeMessage();
    }

    @Test
    @DisplayName("Should catch exception if sending fails")
    void sendEmail_WhenException_ShouldCatch() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP down"));

        emailService.sendEmail("test@user.com", "Subject", "Body");

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should send welcome email")
    void sendWelcomeEmail_ShouldSend() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        UserRegisteredMessage msg = new UserRegisteredMessage();
        msg.setEmail("test@user.com");
        msg.setFirstName("John");
        msg.setLastName("Doe");
        msg.setPhoneNumber("1234567890");
        msg.setRegisteredAt(LocalDateTime.now());

        emailService.sendWelcomeEmail(msg);

        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("Should send policy purchased email")
    void sendPolicyPurchasedEmail_ShouldSend() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        PolicyPurchasedMessage msg = new PolicyPurchasedMessage();
        msg.setCustomerEmail("test@user.com");
        msg.setCustomerName("John Doe");
        msg.setPolicyNumber("POL-123");
        msg.setPolicyName("Health Plan");
        msg.setPolicyType("HEALTH");
        msg.setBasePremium(1000.0);
        msg.setCoverageAmount(500000.0);
        msg.setStartDate(LocalDate.now());
        msg.setEndDate(LocalDate.now().plusYears(1));

        emailService.sendPolicyPurchasedEmail(msg);

        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("Should send policy expiring email (Safe zone)")
    void sendPolicyExpiringEmail_SafeZone_ShouldSend() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        PolicyExpiringMessage msg = new PolicyExpiringMessage();
        msg.setCustomerEmail("test@user.com");
        msg.setCustomerName("John Doe");
        msg.setPolicyNumber("POL-123");
        msg.setPolicyName("Health Plan");
        msg.setEndDate(LocalDate.now().plusDays(15));
        msg.setDaysRemaining(15);

        emailService.sendPolicyExpiringEmail(msg);

        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("Should send policy expiring email (Urgent zone)")
    void sendPolicyExpiringEmail_UrgentZone_ShouldSend() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        PolicyExpiringMessage msg = new PolicyExpiringMessage();
        msg.setCustomerEmail("test@user.com");
        msg.setCustomerName("John Doe");
        msg.setPolicyNumber("POL-123");
        msg.setPolicyName("Health Plan");
        msg.setEndDate(LocalDate.now().plusDays(3));
        msg.setDaysRemaining(3);

        emailService.sendPolicyExpiringEmail(msg);

        verify(mailSender, times(1)).send(mimeMessage);
    }
    
    @Test
    @DisplayName("Should handle null days remaining in expiring email")
    void sendPolicyExpiringEmail_NullDays_ShouldHandle() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        PolicyExpiringMessage msg = new PolicyExpiringMessage();
        msg.setCustomerEmail("test@user.com");
        msg.setCustomerName("John");
        msg.setEndDate(LocalDate.now());
        msg.setDaysRemaining(null);

        emailService.sendPolicyExpiringEmail(msg);

        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("Should send claim submitted email")
    void sendClaimSubmittedEmail_ShouldSend() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        ClaimSubmittedMessage msg = new ClaimSubmittedMessage();
        msg.setCustomerEmail("test@user.com");
        msg.setCustomerName("John Doe");
        msg.setClaimNumber("CLM-123");
        msg.setPolicyNumber("POL-123");
        msg.setClaimedAmount(5000.0);
        msg.setClaimType("MEDICAL");
        msg.setIncidentDate(LocalDate.now().minusDays(2));
        msg.setSubmittedAt(LocalDateTime.now());

        emailService.sendClaimSubmittedEmail(msg);

        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("Should send claim reviewed email (Approved with remarks)")
    void sendClaimReviewedEmail_ApprovedWithRemarks_ShouldSend() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        ClaimReviewedMessage msg = new ClaimReviewedMessage();
        msg.setCustomerEmail("test@user.com");
        msg.setCustomerName("John Doe");
        msg.setClaimNumber("CLM-123");
        msg.setDecision("APPROVED");
        msg.setApprovedAmount(4500.0);
        msg.setAdminRemarks("Looks good");
        msg.setReviewedAt(LocalDateTime.now());

        emailService.sendClaimReviewedEmail(msg);

        verify(mailSender, times(1)).send(mimeMessage);
    }
    
    @Test
    @DisplayName("Should send claim reviewed email (Approved without remarks)")
    void sendClaimReviewedEmail_ApprovedWithoutRemarks_ShouldSend() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        ClaimReviewedMessage msg = new ClaimReviewedMessage();
        msg.setCustomerEmail("test@user.com");
        msg.setCustomerName("John Doe");
        msg.setClaimNumber("CLM-123");
        msg.setDecision("APPROVED");
        msg.setApprovedAmount(4500.0);
        msg.setAdminRemarks(null);
        msg.setReviewedAt(LocalDateTime.now());

        emailService.sendClaimReviewedEmail(msg);

        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("Should send claim reviewed email (Rejected)")
    void sendClaimReviewedEmail_Rejected_ShouldSend() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        ClaimReviewedMessage msg = new ClaimReviewedMessage();
        msg.setCustomerEmail("test@user.com");
        msg.setCustomerName("John Doe");
        msg.setClaimNumber("CLM-123");
        msg.setDecision("REJECTED");
        msg.setAdminRemarks("Missing documents");
        msg.setReviewedAt(LocalDateTime.now());

        emailService.sendClaimReviewedEmail(msg);

        verify(mailSender, times(1)).send(mimeMessage);
    }
    
    @Test
    @DisplayName("Should handle null decision in claim reviewed email")
    void sendClaimReviewedEmail_NullDecision_ShouldSend() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        ClaimReviewedMessage msg = new ClaimReviewedMessage();
        msg.setCustomerEmail("test@user.com");
        msg.setCustomerName("John Doe");
        msg.setClaimNumber("CLM-123");
        msg.setDecision(null);
        msg.setReviewedAt(LocalDateTime.now());

        emailService.sendClaimReviewedEmail(msg);

        verify(mailSender, times(1)).send(mimeMessage);
    }
}
