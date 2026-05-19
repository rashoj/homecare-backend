package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "visit_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitNote {

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

    @Column(columnDefinition = "TEXT")
    private String generalNotes;

    @Column(columnDefinition = "TEXT")
    private String meals;

    @Column(columnDefinition = "TEXT")
    private String medicationNotes;

    @Column(columnDefinition = "TEXT")
    private String mobilityNotes;

    @Column(columnDefinition = "TEXT")
    private String moodNotes;

    @Column(columnDefinition = "TEXT")
    private String hygieneCare;

    @Column(columnDefinition = "TEXT")
    private String safetyConcerns;

    @Column(columnDefinition = "TEXT")
    private String familyUpdate;

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    private Boolean incidentReported;

    @Column(columnDefinition = "TEXT")
    private String incidentDetails;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.incidentReported == null) {
            this.incidentReported = false;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}