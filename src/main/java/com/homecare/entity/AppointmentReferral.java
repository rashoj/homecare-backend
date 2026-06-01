package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment_referrals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentReferral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Existing client if already in system
     */
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    /*
     * Caregiver who submitted referral
     */
    @ManyToOne
    @JoinColumn(name = "caregiver_id", nullable = false)
    private User caregiver;

    /*
     * Client info if not already registered
     */
    private String clientFullName;

    private String clientPhone;

    private String clientEmail;

    private String clientAddress;

    /*
     * Referral / hospital info
     */
    private String referralSource;
    // HOSPITAL, FAMILY, SELF_REFERRAL, SOCIAL_WORKER, REHAB_CENTER

    private String hospitalName;

    private String dischargePlannerName;

    private String dischargePlannerPhone;

    /*
     * Requested appointment
     */
    private LocalDateTime requestedStartTime;

    private LocalDateTime requestedEndTime;

    private String serviceType;
    // PERSONAL_CARE, COMPANION, ADL_ASSISTANCE

    @Column(columnDefinition = "TEXT")
    private String notes;

    /*
     * Admin workflow
     */
    private String status;
    // SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, CONVERTED

    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    /*
     * Converted appointment if approved
     */
    @OneToOne
    @JoinColumn(name = "converted_appointment_id")
    private Appointment convertedAppointment;

    private LocalDateTime reviewedAt;

    @ManyToOne
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = "SUBMITTED";
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}