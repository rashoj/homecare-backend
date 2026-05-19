package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ServiceDocumentationResponse {

    private Long id;

    private Long appointmentId;

    private Long clientId;

    private String clientName;

    private Long caregiverId;

    private String caregiverName;

    private String shiftTasksCompleted;

    private String adlsCompleted;

    private String goalProgressNotes;

    private String dailyServiceNotes;

    private Boolean shiftCompleted;

    private String caregiverSignature;

    private String status;

    private Boolean locked;

    private String supervisorComments;

    private LocalDateTime submittedAt;

    private LocalDateTime approvedAt;
}