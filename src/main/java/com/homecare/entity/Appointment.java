package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "caregiver_id")
    private User caregiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String serviceType;
    // PERSONAL_CARE, COMPANION, MEDICATION_REMINDER,
    // TRANSPORTATION, ADL_ASSISTANCE, BEHAVIORAL_SUPPORT

    private String shiftType;
    // REGULAR, PRN, FILL_IN, OPEN_SHIFT

    private String status;
    // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, MISSED

    private Boolean evvRequired;

    private Boolean billable;

    private String repeatType;

    private LocalDateTime recurringGroupCreatedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private Boolean completed;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.completed == null) {
            this.completed = false;
        }

        if (this.status == null) {
            this.status = "SCHEDULED";
        }

        if (this.shiftType == null) {
            this.shiftType = "REGULAR";
        }

        if (this.evvRequired == null) {
            this.evvRequired = true;
        }

        if (this.billable == null) {
            this.billable = true;
        }
        if (this.repeatType == null) {
            this.repeatType = "NONE";
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
