package com.homecare.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NotNull
@NotBlank
@Size
@FutureOrPresent
public class BehaviorEventRequest {

    private Long clientId;
    private Long caregiverId;
    private Long appointmentId;
    private Long serviceDocumentationId;

    private String behaviorType;
    private String trigger;
    private String severity;
    private Integer durationMinutes;
    private String interventionUsed;
    private String outcome;
    private String notes;
}