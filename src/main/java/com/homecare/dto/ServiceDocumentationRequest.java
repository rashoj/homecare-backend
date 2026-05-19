package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceDocumentationRequest {

    private Long appointmentId;

    private String shiftTasksCompleted;

    private String adlsCompleted;

    private String goalProgressNotes;

    private String dailyServiceNotes;

    private Boolean shiftCompleted;

    private String caregiverSignature;
}