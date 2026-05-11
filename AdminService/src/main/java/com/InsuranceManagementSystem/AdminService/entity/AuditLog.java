package com.InsuranceManagementSystem.AdminService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String adminEmail;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String resourceType;

    @Column(nullable = false)
    private String resourceId;

    @Column(length = 1000)
    private String details;

    @Column(nullable = false)
    private String result;

    @Column(length = 500)
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime performedAt;

    @Column
    private String ipAddress;

    @PrePersist
    public void prePersist() {
        this.performedAt = LocalDateTime.now();
    }
}