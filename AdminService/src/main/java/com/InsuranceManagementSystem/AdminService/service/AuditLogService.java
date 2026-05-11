package com.InsuranceManagementSystem.AdminService.service;

import com.InsuranceManagementSystem.AdminService.entity.AuditLog;
import com.InsuranceManagementSystem.AdminService.enums.AuditAction;
import com.InsuranceManagementSystem.AdminService.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for creating, managing, and retrieving system audit logs.
 * Audit logs record all significant administrative actions for compliance
 * and tracing purposes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Records a successful administrative action in the audit log.
     *
     * @param adminEmail   The email of the administrator who performed the action.
     * @param action       The specific {@link AuditAction} performed.
     * @param resourceType The type of resource affected (e.g., USER, CLAIM, POLICY).
     * @param resourceId   The unique identifier of the affected resource.
     * @param details      Additional details or remarks about the action.
     */
    public void logSuccess(
            String adminEmail,
            AuditAction action,
            String resourceType,
            String resourceId,
            String details
    ) {
        AuditLog log = AuditLog.builder()
                .adminEmail(adminEmail)
                .action(action.name())
                .resourceType(resourceType)
                .resourceId(resourceId)
                .details(details)
                .result("SUCCESS")
                .build();

        auditLogRepository.save(log);
        this.log.info("Audit log saved: {} {} {} by {}",
                action, resourceType, resourceId, adminEmail);
    }

    /**
     * Records a failed administrative action in the audit log.
     *
     * @param adminEmail    The email of the administrator who attempted the action.
     * @param action        The specific {@link AuditAction} attempted.
     * @param resourceType  The type of resource targeted.
     * @param resourceId    The unique identifier of the targeted resource.
     * @param failureReason The reason the action failed (e.g., exception message).
     */
    public void logFailure(
            String adminEmail,
            AuditAction action,
            String resourceType,
            String resourceId,
            String failureReason
    ) {
        AuditLog auditLog = AuditLog.builder()
                .adminEmail(adminEmail)
                .action(action.name())
                .resourceType(resourceType)
                .resourceId(resourceId)
                .details("Action failed")
                .result("FAILED")
                .failureReason(failureReason)
                .build();

        auditLogRepository.save(auditLog);
        log.warn("Audit log saved (FAILED): {} {} {} by {} - Reason: {}",
                action, resourceType, resourceId,
                adminEmail, failureReason);
    }

    /**
     * Retrieves all recorded audit logs, sorted by the time they were performed (descending).
     *
     * @return List of all {@link AuditLog} entries.
     */
    public List<AuditLog> getAllLogs() {
        return auditLogRepository
                .findAll()
                .stream()
                .sorted((a, b) -> b.getPerformedAt()
                        .compareTo(a.getPerformedAt()))
                .toList();
    }

    /**
     * Retrieves audit logs specifically performed by a certain administrator.
     *
     * @param adminEmail The email of the administrator.
     * @return List of {@link AuditLog} entries for the specified admin.
     */
    public List<AuditLog> getLogsByAdmin(String adminEmail) {
        return auditLogRepository
                .findByAdminEmailOrderByPerformedAtDesc(adminEmail);
    }

    /**
     * Retrieves audit logs related to a specific resource.
     *
     * @param resourceId The unique identifier of the resource.
     * @return List of {@link AuditLog} entries targeting the specified resource.
     */
    public List<AuditLog> getLogsByResource(String resourceId) {
        return auditLogRepository
                .findByResourceIdOrderByPerformedAtDesc(resourceId);
    }

    /**
     * Retrieves the 20 most recent audit logs.
     *
     * @return List of the top 20 recent {@link AuditLog} entries.
     */
    public List<AuditLog> getRecentLogs() {
        return auditLogRepository
                .findTop20ByOrderByPerformedAtDesc();
    }

    /**
     * Retrieves audit logs that occurred within a specific date range.
     *
     * @param startDate The beginning of the date range.
     * @param endDate   The end of the date range.
     * @return List of {@link AuditLog} entries within the specified range.
     */
    public List<AuditLog> getLogsInDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        return auditLogRepository
                .findLogsInDateRange(startDate, endDate);
    }
}