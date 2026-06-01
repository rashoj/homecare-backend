package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AppointmentReferralResponse {

    private Long id;

    private Long clientId;
    private String clientName;

    private Long caregiverId;
    private String caregiverName;

    private String clientFullName;
    private String clientPhone;
    private String clientEmail;
    private String clientAddress;

    private String referralSource;
    private String hospitalName;
    private String dischargePlannerName;
    private String dischargePlannerPhone;

    private LocalDateTime requestedStartTime;
    private LocalDateTime requestedEndTime;

    private String serviceType;
    private String notes;

    private String status;
    private String adminNotes;

    private Long convertedAppointmentId;

    private LocalDateTime reviewedAt;
    private String reviewedByName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}