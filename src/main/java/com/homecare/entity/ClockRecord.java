package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "clock_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClockRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    private LocalDateTime clockInTime;

    private LocalDateTime clockOutTime;

    private Double clockInLatitude;

    private Double clockInLongitude;

    private Double clockOutLatitude;

    private Double clockOutLongitude;

    private Double totalHours;

    private String status;

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
        this.status = "CLOCKED_IN";
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();

        if (clockInTime != null && clockOutTime != null) {
            long minutes = Duration.between(clockInTime, clockOutTime).toMinutes();
            this.totalHours = minutes / 60.0;
        }
    }
}