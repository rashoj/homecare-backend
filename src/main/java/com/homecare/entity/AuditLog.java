package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long actorUserId;

    private String actorName;

    private String actorRole;

    private Long clientId;

    private String action;
    // VIEW_CLIENT, CREATE_APPOINTMENT, REVIEW_REFERRAL, APPROVE_RESCHEDULE, etc.

    private String resourceType;
    // CLIENT, APPOINTMENT, REFERRAL, RESCHEDULE_REQUEST, MAR, VISIT_NOTE, DOCUMENT

    private Long resourceId;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String ipAddress;

    private String userAgent;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}