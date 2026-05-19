package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ClockRecordResponse {

    private Long id;

    private Long appointmentId;

    private String clientName;

    private String caregiverName;

    private LocalDateTime clockInTime;

    private LocalDateTime clockOutTime;

    private Double clockInLatitude;

    private Double clockInLongitude;

    private Double clockOutLatitude;

    private Double clockOutLongitude;

    private Double totalHours;

    private String status;

    private String clockInNotes;

    private String clockOutNotes;
}