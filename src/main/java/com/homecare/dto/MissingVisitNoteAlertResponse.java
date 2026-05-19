package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MissingVisitNoteAlertResponse {

    private Long appointmentId;

    private Long clientId;
    private String clientName;

    private Long caregiverId;
    private String caregiverName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String status;
    private Boolean completed;
}