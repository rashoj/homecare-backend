package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AppointmentResponse {

    private Long id;

    private Long clientId;

    private String clientName;

    private Long caregiverId;

    private String caregiverName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String serviceType;

    private String shiftType;

    private String status;

    private Boolean evvRequired;

    private Boolean billable;

    private String notes;

    private Boolean completed;
}