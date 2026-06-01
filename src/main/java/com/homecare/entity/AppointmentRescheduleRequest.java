package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment_reschedule_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentRescheduleRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Appointment appointment;

    @ManyToOne(optional = false)
    private Client client;

    @ManyToOne(optional = false)
    private User caregiver;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requested_by_user_id")
    private User requestedBy;

    private LocalDateTime originalStartTime;
    private LocalDateTime originalEndTime;

    private LocalDateTime requestedStartTime;
    private LocalDateTime requestedEndTime;

    @Column(columnDefinition = "TEXT")
    private String reason;

    private String status;
    // SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED

    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    @ManyToOne
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedBy;

    private LocalDateTime reviewedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) {
            status = "SUBMITTED";
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}