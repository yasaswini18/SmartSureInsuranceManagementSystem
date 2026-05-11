package com.InsuranceManagementSystem.AdminService.repository;

import com.InsuranceManagementSystem.AdminService.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByAdminEmailOrderByPerformedAtDesc(String adminEmail);

    List<AuditLog> findByResourceIdOrderByPerformedAtDesc(String resourceId);

    List<AuditLog> findByActionOrderByPerformedAtDesc(String action);

    List<AuditLog> findByResourceTypeOrderByPerformedAtDesc(String resourceType);

    @Query("SELECT a FROM AuditLog a WHERE a.performedAt BETWEEN :startDate AND :endDate ORDER BY a.performedAt DESC")
    List<AuditLog> findLogsInDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT a FROM AuditLog a WHERE a.adminEmail = :adminEmail AND a.performedAt BETWEEN :startDate AND :endDate ORDER BY a.performedAt DESC")
    List<AuditLog> findLogsByAdminInDateRange(
            @Param("adminEmail") String adminEmail,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<AuditLog> findByResultOrderByPerformedAtDesc(String result);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action = :action AND a.performedAt BETWEEN :startDate AND :endDate")
    long countActionInDateRange(
            @Param("action") String action,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<AuditLog> findTop20ByOrderByPerformedAtDesc();
}