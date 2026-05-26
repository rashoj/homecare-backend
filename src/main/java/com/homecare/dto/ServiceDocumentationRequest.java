package com.homecare.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NotNull
@NotBlank
@Size
@FutureOrPresent
public class ServiceDocumentationRequest {

    private Long appointmentId;

    private String shiftTasksCompleted;

    private String adlsCompleted;

    private String goalProgressNotes;

    private String dailyServiceNotes;

    private Boolean shiftCompleted;

    private String caregiverSignature;

    private List<ISPGoalProgressSubmissionRequest> ispGoalProgress;

    private List<BehaviorEventSubmissionRequest> behaviorEvents;
}