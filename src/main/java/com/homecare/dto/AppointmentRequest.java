package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AppointmentRequest {

    private Long clientId;

    private Long caregiverId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String serviceType;

    private String shiftType;

    private String status;

    private Boolean evvRequired;

    private Boolean billable;

    private String notes;
}