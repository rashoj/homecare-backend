package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EVVExceptionResponse {

    private Long id;

    private Long appointmentId;
    private Long clockRecordId;

    private Long clientId;
    private String clientName;

    private Long caregiverId;
    private String caregiverName;

    private String exceptionType;
    private String severity;
    private String status;

    private String description;
    private String supervisorNotes;

    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;

    private String adminResolutionReason;
    private LocalDateTime correctedClockOutTime;
    private Boolean adminApproved;
}