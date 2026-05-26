package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ISPGoalProgressSubmissionRequest {

    private Long goalId;

    private String progressStatus;
    // IMPROVED, MAINTAINED, REGRESSED, NOT_ADDRESSED

    private String promptLevel;
    // INDEPENDENT, VERBAL_PROMPT, PHYSICAL_ASSIST, FULL_ASSIST

    private String progressNote;
}