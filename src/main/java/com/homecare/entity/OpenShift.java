package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "open_shifts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claimed_by_caregiver_id")
    private User claimedByCaregiver;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String serviceType;
    private String shiftType;

    private String status;
    // OPEN, CLAIMED, ASSIGNED, CANCELLED

    private String priority;
    // LOW, NORMAL, HIGH, URGENT

    private Boolean evvRequired;
    private Boolean billable;

    @Column(columnDefinition = "TEXT")
    private String requiredSkills;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private Boolean assignedCaregiverOnly;

    private LocalDateTime expiresAt;

    private Long createdByUserId;
    private LocalDateTime claimedAt;
    private LocalDateTime assignedAt;

    private Long appointmentId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) status = "OPEN";
        if (priority == null) priority = "NORMAL";
        if (shiftType == null) shiftType = "OPEN_SHIFT";
        if (evvRequired == null) evvRequired = true;
        if (billable == null) billable = true;
        if (assignedCaregiverOnly == null) assignedCaregiverOnly = false;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}