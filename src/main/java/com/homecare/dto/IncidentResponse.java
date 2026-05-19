package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class IncidentResponse {

    private Long id;

    private Long appointmentId;

    private Long clientId;
    private String clientName;

    private Long caregiverId;
    private String caregiverName;

    private LocalDateTime incidentDateTime;

    private String incidentType;
    private String severity;

    private String description;
    private String immediateActionTaken;

    private String witnessName;
    private String witnessPhone;
    private String witnessStatement;

    private String status;
    private Boolean stateReportable;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String reviewStatus;
    private String supervisorNotes;
    private String correctiveAction;
    private String followUpRequired;
}