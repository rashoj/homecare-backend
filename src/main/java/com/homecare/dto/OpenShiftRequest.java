package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OpenShiftRequest {

    private Long clientId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String serviceType;
    private String shiftType;

    private String priority;

    private Boolean evvRequired;
    private Boolean billable;

    private String requiredSkills;
    private String notes;
    private Boolean assignedCaregiverOnly;
    private LocalDateTime expiresAt;

}