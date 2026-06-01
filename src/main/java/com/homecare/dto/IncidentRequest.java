package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class IncidentRequest {

    private Long appointmentId;
    private Long clientId;
    private Long caregiverId;

    private LocalDateTime incidentDateTime;

    private String incidentType;
    private String severity;

    private String description;
    private String immediateActionTaken;

    private String witnessName;
    private String witnessPhone;
    private String witnessStatement;

    private Boolean stateReportable;
    private Long actorUserId;
}