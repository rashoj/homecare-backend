package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class OpenShiftResponse {

    private Long id;

    private Long organizationId;

    private Long clientId;
    private String clientName;

    private Long claimedByCaregiverId;
    private String claimedByCaregiverName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String serviceType;
    private String shiftType;
    private String status;
    private String priority;

    private Boolean evvRequired;
    private Boolean billable;

    private String requiredSkills;
    private String notes;

    private Long createdByUserId;
    private LocalDateTime claimedAt;
    private LocalDateTime assignedAt;

    private Long appointmentId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Boolean assignedCaregiverOnly;
    private LocalDateTime expiresAt;
}