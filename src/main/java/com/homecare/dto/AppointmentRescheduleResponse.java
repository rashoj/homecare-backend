package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AppointmentRescheduleResponse {

    private Long id;

    private Long appointmentId;

    private Long clientId;
    private String clientName;

    private Long caregiverId;
    private String caregiverName;

    private Long requestedByUserId;
    private String requestedByName;

    private LocalDateTime originalStartTime;
    private LocalDateTime originalEndTime;

    private LocalDateTime requestedStartTime;
    private LocalDateTime requestedEndTime;

    private String reason;
    private String status;
    private String adminNotes;

    private Long reviewedByUserId;
    private String reviewedByName;
    private LocalDateTime reviewedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}