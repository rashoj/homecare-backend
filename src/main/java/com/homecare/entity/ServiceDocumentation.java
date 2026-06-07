package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_documentations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceDocumentation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "caregiver_id", nullable = false)
    private User caregiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(columnDefinition = "TEXT")
    private String shiftTasksCompleted;

    @Column(columnDefinition = "TEXT")
    private String adlsCompleted;

    @Column(columnDefinition = "TEXT")
    private String goalProgressNotes;

    @Column(columnDefinition = "TEXT")
    private String dailyServiceNotes;

    private Boolean shiftCompleted;

    @Column(columnDefinition = "TEXT")
    private String caregiverSignature;

    private String status;
    // SUBMITTED, APPROVED, REJECTED

    private Boolean locked;

    @Column(columnDefinition = "TEXT")
    private String supervisorComments;

    private LocalDateTime submittedAt;

    private LocalDateTime approvedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime correctedClockInTime;

    private LocalDateTime correctedClockOutTime;

    @Column(columnDefinition = "TEXT")
    private String correctionReason;

    private Boolean timeCorrectionApproved;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = "SUBMITTED";
        }

        if (this.locked == null) {
            this.locked = false;
        }

        if (this.shiftCompleted == null) {
            this.shiftCompleted = false;
        }

        if (this.submittedAt == null) {
            this.submittedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}