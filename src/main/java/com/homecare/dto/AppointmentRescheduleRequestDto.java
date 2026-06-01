package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AppointmentRescheduleRequestDto {

    private Long appointmentId;
    private Long requestedByUserId;

    private LocalDateTime requestedStartTime;
    private LocalDateTime requestedEndTime;

    private String reason;
}