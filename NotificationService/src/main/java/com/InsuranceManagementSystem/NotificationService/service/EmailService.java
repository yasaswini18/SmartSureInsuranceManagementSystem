package com.InsuranceManagementSystem.NotificationService.service;

import com.InsuranceManagementSystem.NotificationService.dtos.MessageDTOs.*;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Service class responsible for sending formatted HTML emails.
 * Handles the construction of various notification emails using predefined templates and styles.
 */
@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String PRIMARY_COLOR = "#1a56db";
    private static final String SUCCESS_COLOR = "#10b981";
    private static final String WARNING_COLOR = "#f59e0b";
    private static final String DANGER_COLOR = "#ef4444";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");

    /**
     * Sends a generic HTML email to the specified recipient.
     * 
     * @param toEmail The recipient's email address.
     * @param subject The subject line of the email.
     * @param htmlBody The HTML content of the email body.
     */
    public void sendEmail(String toEmail, String subject, String htmlBody) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            log.warn("Email address missing, skipping email sending");
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String getHeader() {
        return "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e5e7eb; border-radius: 8px; padding: 20px;\">" +
               "<h1 style=\"color: " + PRIMARY_COLOR + "; margin-bottom: 10px; text-align: center;\">SmartSure Insurance</h1>" +
               "<hr style=\"border: none; border-top: 1px solid #e5e7eb; margin-bottom: 20px;\">";
    }

    private String getFooter() {
        return "<hr style=\"border: none; border-top: 1px solid #e5e7eb; margin-top: 30px; margin-bottom: 20px;\">" +
               "<p style=\"color: #6b7280; font-size: 12px; text-align: center;\">This is an automated message from SmartSure Insurance. Please do not reply.</p>" +
               "</div>";
    }

    private String getTableStart() {
        return "<table style=\"width: 100%; border-collapse: collapse; margin: 20px 0;\">";
    }

    private String getTableRow(String label, String value) {
        return "<tr><td style=\"padding: 8px; border-bottom: 1px solid #e5e7eb; font-weight: bold; width: 40%;\">" + label + "</td>" +
               "<td style=\"padding: 8px; border-bottom: 1px solid #e5e7eb;\">" + value + "</td></tr>";
    }

    private String getTableEnd() {
        return "</table>";
    }

    /**
     * Sends a welcome email to a newly registered user.
     * 
     * @param message The user registration event data.
     */
    public void sendWelcomeEmail(UserRegisteredMessage message) {
        String subject = "Welcome to SmartSure Insurance — Account Created Successfully";
        String fullName = message.getFirstName() + " " + message.getLastName();
        String toEmail = message.getEmail();
        
        String body = getHeader() +
                "<p>Hello " + message.getFirstName() + ",</p>" +
                "<p>Your SmartSure account has been created successfully.</p>" +
                getTableStart() +
                getTableRow("Name", fullName) +
                getTableRow("Email", toEmail) +
                getTableRow("Phone", message.getPhoneNumber()) +
                getTableRow("Registered On", message.getRegisteredAt().format(DATETIME_FORMATTER)) +
                getTableEnd() +
                "<p style=\"font-weight: bold; color: " + PRIMARY_COLOR + ";\">You can now browse and purchase insurance policies.</p>" +
                getFooter();
                
        sendEmail(toEmail, subject, body);
    }

    /**
     * Sends a confirmation email for a successfully purchased policy.
     * 
     * @param message The policy purchase event data.
     */
    public void sendPolicyPurchasedEmail(PolicyPurchasedMessage message) {
        String subject = "Policy Purchase Confirmed — " + message.getPolicyNumber();
        String toEmail = message.getCustomerEmail();
        
        String body = getHeader() +
                "<p>Hello " + message.getCustomerName() + ",</p>" +
                "<p>Your insurance policy has been purchased successfully.</p>" +
                getTableStart() +
                getTableRow("Policy Number", message.getPolicyNumber()) +
                getTableRow("Plan Name", message.getPolicyName()) +
                getTableRow("Policy Type", message.getPolicyType()) +
                getTableRow("Base Premium", "Rs. " + message.getBasePremium() + "/month") +
                getTableRow("Coverage Amount", "Rs. " + message.getCoverageAmount()) +
                getTableRow("Start Date", message.getStartDate().format(DATE_FORMATTER)) +
                getTableRow("End Date", message.getEndDate().format(DATE_FORMATTER)) +
                getTableEnd() +
                "<p>Please keep your policy number safe for future reference and claims.</p>" +
                getFooter();
                
        sendEmail(toEmail, subject, body);
    }

    /**
     * Sends an expiry reminder email for a policy approaching its end date.
     * The email template highlights urgency based on the remaining days.
     * 
     * @param message The policy expiring event data.
     */
    public void sendPolicyExpiringEmail(PolicyExpiringMessage message) {
        String subject = "Action Required — Your Policy " + message.getPolicyNumber() + " is Expiring Soon";
        String toEmail = message.getCustomerEmail();
        int daysRemaining = message.getDaysRemaining() != null ? message.getDaysRemaining() : 0;
        
        String bannerColor = WARNING_COLOR;
        String extraMessage = "<p style=\"color: " + WARNING_COLOR + "; font-weight: bold;\">Please consider renewing your policy before it expires.</p>";
        
        if (daysRemaining <= 7) {
            bannerColor = DANGER_COLOR;
            extraMessage = "<p style=\"color: " + DANGER_COLOR + "; font-weight: bold;\">URGENT: Your policy expires very soon. Please renew immediately to avoid losing coverage.</p>";
        }
        
        String body = getHeader() +
                "<p>Hello " + message.getCustomerName() + ",</p>" +
                "<div style=\"background-color: " + bannerColor + "; color: white; padding: 10px; text-align: center; font-weight: bold; border-radius: 4px; margin-bottom: 20px;\">" +
                "Your policy is expiring in " + daysRemaining + " days" +
                "</div>" +
                getTableStart() +
                getTableRow("Policy Number", message.getPolicyNumber()) +
                getTableRow("Plan Name", message.getPolicyName()) +
                getTableRow("Expiry Date", message.getEndDate().format(DATE_FORMATTER)) +
                getTableRow("Days Remaining", String.valueOf(daysRemaining)) +
                getTableEnd() +
                extraMessage +
                "<p style=\"font-weight: bold;\"><a href=\"#\" style=\"color: " + PRIMARY_COLOR + "; text-decoration: none;\">Login to SmartSure to renew your policy.</a></p>" +
                getFooter();
                
        sendEmail(toEmail, subject, body);
    }

    /**
     * Sends an acknowledgment email when a customer submits an insurance claim.
     * 
     * @param message The claim submission event data.
     */
    public void sendClaimSubmittedEmail(ClaimSubmittedMessage message) {
        String subject = "Claim Received — " + message.getClaimNumber() + " — We Are Processing Your Request";
        String toEmail = message.getCustomerEmail();
        
        String body = getHeader() +
                "<p>Hello " + message.getCustomerName() + ",</p>" +
                "<p>We have received your insurance claim. Our team will review it shortly.</p>" +
                getTableStart() +
                getTableRow("Claim Number", message.getClaimNumber()) +
                getTableRow("Policy Number", message.getPolicyNumber()) +
                getTableRow("Claim Amount", "Rs. " + message.getClaimedAmount()) +
                getTableRow("Incident Type", message.getClaimType()) +
                getTableRow("Incident Date", message.getIncidentDate().format(DATE_FORMATTER)) +
                getTableRow("Submitted On", message.getSubmittedAt().format(DATETIME_FORMATTER)) +
                getTableEnd() +
                "<p>You will receive another email once your claim has been reviewed.</p>" +
                "<p>Please keep your claim number <strong>" + message.getClaimNumber() + "</strong> for tracking.</p>" +
                getFooter();
                
        sendEmail(toEmail, subject, body);
    }

    /**
     * Sends a notification email regarding the outcome of a claim review (APPROVED or REJECTED).
     * 
     * @param message The claim review event data.
     */
    public void sendClaimReviewedEmail(ClaimReviewedMessage message) {
        String status = message.getDecision();
        boolean isApproved = "APPROVED".equalsIgnoreCase(status);
        String claimNumber = message.getClaimNumber();
        String toEmail = message.getCustomerEmail();
        
        String subject;
        String bannerColor;
        String bannerText;
        String statusColor;
        
        if (isApproved) {
            subject = "Great News — Your Claim " + claimNumber + " Has Been Approved";
            bannerColor = SUCCESS_COLOR;
            bannerText = "Your Claim Has Been Approved";
            statusColor = SUCCESS_COLOR;
        } else {
            subject = "Update on Your Claim " + claimNumber + " — Action Required";
            bannerColor = DANGER_COLOR;
            bannerText = "Your Claim Could Not Be Approved";
            statusColor = DANGER_COLOR;
        }
        
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append(getHeader());
        bodyBuilder.append("<p>Hello ").append(message.getCustomerName()).append(",</p>");
        bodyBuilder.append("<div style=\"background-color: ").append(bannerColor).append("; color: white; padding: 10px; text-align: center; font-weight: bold; border-radius: 4px; margin-bottom: 20px;\">")
                   .append(bannerText)
                   .append("</div>");
                   
        bodyBuilder.append(getTableStart());
        bodyBuilder.append(getTableRow("Claim Number", claimNumber));
        bodyBuilder.append("<tr><td style=\"padding: 8px; border-bottom: 1px solid #e5e7eb; font-weight: bold; width: 40%;\">Status</td>")
                   .append("<td style=\"padding: 8px; border-bottom: 1px solid #e5e7eb; color: ").append(statusColor).append("; font-weight: bold;\">")
                   .append(status != null ? status.toUpperCase() : "N/A").append("</td></tr>");
                   
        if (isApproved) {
            bodyBuilder.append(getTableRow("Approved Amount", "Rs. " + message.getApprovedAmount()));
        } else {
            bodyBuilder.append(getTableRow("Reason", message.getAdminRemarks() != null ? message.getAdminRemarks() : "N/A"));
        }
        bodyBuilder.append(getTableRow("Reviewed On", message.getReviewedAt().format(DATETIME_FORMATTER)));
        bodyBuilder.append(getTableEnd());
        
        if (isApproved) {
            if (message.getAdminRemarks() != null && !message.getAdminRemarks().trim().isEmpty()) {
                bodyBuilder.append("<p><strong>Remarks:</strong> ").append(message.getAdminRemarks()).append("</p>");
            }
            bodyBuilder.append("<p>The approved amount will be processed as per your policy terms.</p>");
        } else {
            bodyBuilder.append("<p>If you have questions about this decision, please contact our support team.</p>");
        }
        
        bodyBuilder.append(getFooter());
        
        sendEmail(toEmail, subject, bodyBuilder.toString());
    }
}
