package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "medication_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caregiver_id")
    private User caregiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    private LocalDateTime scheduledAt;

    private LocalDateTime givenAt;

    private String status;
    // GIVEN, MISSED, REFUSED, HELD, PRN_GIVEN

    @Column(columnDefinition = "TEXT")
    private String notes;

    private Boolean prn;

    @Column(columnDefinition = "TEXT")
    private String prnReason;

    @Column(columnDefinition = "TEXT")
    private String refusalReason;

    @Column(columnDefinition = "TEXT")
    private String missedReason;

    @Column(columnDefinition = "TEXT")
    private String caregiverSignature;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.givenAt == null &&
                ("GIVEN".equalsIgnoreCase(this.status)
                        || "ADMINISTERED".equalsIgnoreCase(this.status)
                        || "PRN_GIVEN".equalsIgnoreCase(this.status))) {
            this.givenAt = LocalDateTime.now();
        }

        if (this.prn == null) {
            this.prn = false;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}