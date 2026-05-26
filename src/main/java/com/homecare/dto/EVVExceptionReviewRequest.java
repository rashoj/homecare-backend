package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EVVExceptionReviewRequest {

    private String status;
    // REVIEWED, RESOLVED

    private String supervisorNotes;

    private String adminResolutionReason;
    private LocalDateTime correctedClockOutTime;
    private Boolean adminApproved;
}