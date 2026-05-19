package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "caregiver_id", nullable = false)
    private User caregiver;

    private LocalDateTime incidentDateTime;

    private String incidentType;

    private String severity;
    // LOW, MEDIUM, HIGH, CRITICAL

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String immediateActionTaken;

    private String witnessName;

    private String witnessPhone;

    @Column(columnDefinition = "TEXT")
    private String witnessStatement;

    private String status;
    // SUBMITTED, UNDER_REVIEW, RESOLVED, CLOSED

    private Boolean stateReportable;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.incidentDateTime == null) {
            this.incidentDateTime = LocalDateTime.now();
        }

        if (this.status == null) {
            this.status = "SUBMITTED";
        }

        if (this.stateReportable == null) {
            this.stateReportable = false;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}