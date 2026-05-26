package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_documentation_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceDocumentationAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long documentationId;

    private String oldStatus;
    private String newStatus;

    private LocalDateTime oldClockInTime;
    private LocalDateTime oldClockOutTime;

    private LocalDateTime correctedClockInTime;
    private LocalDateTime correctedClockOutTime;

    @Column(columnDefinition = "TEXT")
    private String correctionReason;

    @Column(columnDefinition = "TEXT")
    private String supervisorComments;

    private Boolean timeCorrectionApproved;

    private Long reviewedBy;

    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}