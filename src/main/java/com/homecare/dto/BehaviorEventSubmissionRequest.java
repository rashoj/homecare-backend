package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BehaviorEventSubmissionRequest {

    private String behaviorType;
    private String trigger;
    private String severity;
    private Integer durationMinutes;
    private String interventionUsed;
    private String outcome;
    private String notes;
}