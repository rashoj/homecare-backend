package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "timesheets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "clock_record_id", nullable = false, unique = true)
    private ClockRecord clockRecord;

    @ManyToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "caregiver_id", nullable = false)
    private User caregiver;

    @ManyToOne
    @JoinColumn(name = "authorization_id")
    private ClientAuthorization authorization;

    private LocalDateTime clockInTime;

    private LocalDateTime clockOutTime;

    private Double totalHours;

    private Double regularHours;

    private Double overtimeHours;

    private Double caregiverPayRate;

    private Double caregiverPayAmount;

    private Double billingRate;

    private Double billableAmount;

    private String payrollStatus;
    // PENDING, APPROVED, PAID

    private String billingStatus;
    // PENDING, APPROVED, BILLED, DENIED

    private Boolean documentationApproved;

    private Boolean authorizationValid;

    private Boolean billable;

    private Boolean authorizationOverride;

    @Column(columnDefinition = "TEXT")
    private String authorizationOverrideReason;

    private LocalDateTime authorizationOverrideAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime reviewedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.payrollStatus == null) {
            this.payrollStatus = "PENDING";
        }

        if (this.billingStatus == null) {
            this.billingStatus = "PENDING";
        }

        if (this.billable == null) {
            this.billable = false;
        }

        if (this.documentationApproved == null) {
            this.documentationApproved = false;
        }

        if (this.authorizationValid == null) {
            this.authorizationValid = false;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}