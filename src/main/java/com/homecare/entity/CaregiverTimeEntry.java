package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "caregiver_time_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaregiverTimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caregiver_id", nullable = false)
    private User caregiver;

    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;

    private Double clockInLatitude;
    private Double clockInLongitude;

    private Double clockOutLatitude;
    private Double clockOutLongitude;

    private Double totalHours;

    private String shiftType;
    // REGULAR_SHIFT, OPEN_SHIFT, TRAINING, ADMIN_WORK, TRAVEL_TIME

    private String status;
    // CLOCKED_IN, CLOCKED_OUT

    @Column(columnDefinition = "TEXT")
    private String clockInNotes;

    @Column(columnDefinition = "TEXT")
    private String clockOutNotes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = "CLOCKED_IN";
        }

        if (this.shiftType == null) {
            this.shiftType = "REGULAR_SHIFT";
        }

        calculateTotalHours();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateTotalHours();
    }

    private void calculateTotalHours() {
        if (clockInTime != null && clockOutTime != null) {
            long minutes = Duration.between(clockInTime, clockOutTime).toMinutes();
            this.totalHours = minutes / 60.0;
        }
    }
}