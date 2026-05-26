package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "evv_exceptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EVVException {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "clock_record_id")
    private ClockRecord clockRecord;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "caregiver_id", nullable = false)
    private User caregiver;

    private String exceptionType;
    // LATE_CLOCK_IN, EARLY_CLOCK_OUT, MISSED_VISIT, GPS_MISSING, MANUAL_REVIEW

    private String severity;
    // LOW, MEDIUM, HIGH

    private String status;
    // OPEN, REVIEWED, RESOLVED

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String supervisorNotes;

    private LocalDateTime reviewedAt;

    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String adminResolutionReason;

    private LocalDateTime correctedClockOutTime;

    private Boolean adminApproved;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = "OPEN";
        }

        if (this.severity == null) {
            this.severity = "MEDIUM";
        }
    }
}