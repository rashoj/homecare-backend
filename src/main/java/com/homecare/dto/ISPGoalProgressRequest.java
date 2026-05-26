package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ISPGoalProgressRequest {

    private Long goalId;
    private Long clientId;
    private Long caregiverId;
    private Long appointmentId;
    private Long serviceDocumentationId;

    private String progressStatus;
    private String promptLevel;
    private String progressNote;
}