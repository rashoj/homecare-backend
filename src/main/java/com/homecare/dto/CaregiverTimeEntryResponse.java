package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CaregiverTimeEntryResponse {

    private Long id;

    private Long caregiverId;
    private String caregiverName;

    private Long organizationId;

    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;

    private Double totalHours;

    private String shiftType;
    private String status;

    private String clockInNotes;
    private String clockOutNotes;
}