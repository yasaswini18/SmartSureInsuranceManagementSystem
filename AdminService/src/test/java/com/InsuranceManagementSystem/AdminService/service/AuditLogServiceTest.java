package com.InsuranceManagementSystem.AdminService.service;

import com.InsuranceManagementSystem.AdminService.entity.AuditLog;
import com.InsuranceManagementSystem.AdminService.enums.AuditAction;
import com.InsuranceManagementSystem.AdminService.repository.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    @DisplayName("Should save SUCCESS audit log correctly")
    void logSuccess_ShouldSaveCorrectAuditLog() {
        ArgumentCaptor<AuditLog> logCaptor =
                ArgumentCaptor.forClass(AuditLog.class);

        auditLogService.logSuccess(
                "admin@insurance.com",
                AuditAction.APPROVE_CLAIM,
                "CLAIM",
                "CLM-2024-000001",
                "Approved claim for ₹45,000"
        );

        verify(auditLogRepository).save(logCaptor.capture());

        AuditLog saved = logCaptor.getValue();
        assertThat(saved.getAdminEmail())
                .isEqualTo("admin@insurance.com");
        assertThat(saved.getAction())
                .isEqualTo("APPROVE_CLAIM");
        assertThat(saved.getResourceType())
                .isEqualTo("CLAIM");
        assertThat(saved.getResourceId())
                .isEqualTo("CLM-2024-000001");
        assertThat(saved.getResult())
                .isEqualTo("SUCCESS");
        assertThat(saved.getFailureReason()).isNull();
    }

    @Test
    @DisplayName("Should save FAILED audit log correctly")
    void logFailure_ShouldSaveCorrectAuditLog() {
        ArgumentCaptor<AuditLog> logCaptor =
                ArgumentCaptor.forClass(AuditLog.class);

        auditLogService.logFailure(
                "admin@insurance.com",
                AuditAction.APPROVE_CLAIM,
                "CLAIM",
                "CLM-2024-000001",
                "Claims service unavailable"
        );

        verify(auditLogRepository).save(logCaptor.capture());

        AuditLog saved = logCaptor.getValue();
        assertThat(saved.getResult()).isEqualTo("FAILED");
        assertThat(saved.getFailureReason())
                .isEqualTo("Claims service unavailable");
    }

    @Test
    @DisplayName("Should return recent logs")
    void getRecentLogs_ShouldReturnTop20() {
        when(auditLogRepository
                .findTop20ByOrderByPerformedAtDesc())
                .thenReturn(List.of(new AuditLog()));

        List<AuditLog> logs = auditLogService.getRecentLogs();

        assertThat(logs).hasSize(1);
        verify(auditLogRepository)
                .findTop20ByOrderByPerformedAtDesc();
    }

    @Test
    @DisplayName("Should return all logs sorted by date descending")
    void getAllLogs_ShouldReturnSorted() {
        AuditLog log1 = new AuditLog();
        log1.setPerformedAt(LocalDateTime.now().minusDays(1));
        
        AuditLog log2 = new AuditLog();
        log2.setPerformedAt(LocalDateTime.now());
        
        when(auditLogRepository.findAll()).thenReturn(List.of(log1, log2));

        List<AuditLog> logs = auditLogService.getAllLogs();

        assertThat(logs).hasSize(2);
        assertThat(logs.get(0).getPerformedAt()).isAfter(logs.get(1).getPerformedAt());
    }

    @Test
    @DisplayName("Should return logs by admin email")
    void getLogsByAdmin_ShouldReturnLogs() {
        when(auditLogRepository.findByAdminEmailOrderByPerformedAtDesc("admin@insurance.com"))
                .thenReturn(List.of(new AuditLog()));

        List<AuditLog> logs = auditLogService.getLogsByAdmin("admin@insurance.com");

        assertThat(logs).hasSize(1);
    }

    @Test
    @DisplayName("Should return logs by resource id")
    void getLogsByResource_ShouldReturnLogs() {
        when(auditLogRepository.findByResourceIdOrderByPerformedAtDesc("1"))
                .thenReturn(List.of(new AuditLog()));

        List<AuditLog> logs = auditLogService.getLogsByResource("1");

        assertThat(logs).hasSize(1);
    }

    @Test
    @DisplayName("Should return logs in date range")
    void getLogsInDateRange_ShouldReturnLogs() {
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now();
        when(auditLogRepository.findLogsInDateRange(start, end))
                .thenReturn(List.of(new AuditLog()));

        List<AuditLog> logs = auditLogService.getLogsInDateRange(start, end);

        assertThat(logs).hasSize(1);
    }
}