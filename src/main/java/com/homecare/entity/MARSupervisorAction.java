package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mar_supervisor_actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MARSupervisorAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_log_id", nullable = false)
    private MedicationLog medicationLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private User supervisor;

    private String actionStatus;
    // ACKNOWLEDGED, FOLLOW_UP_REQUIRED, ESCALATED, RESOLVED

    @Column(columnDefinition = "TEXT")
    private String supervisorNotes;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}