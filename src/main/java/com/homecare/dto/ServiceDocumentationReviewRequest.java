package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ServiceDocumentationReviewRequest {

    private String status;

    private String supervisorComments;

    private LocalDateTime correctedClockInTime;
    private LocalDateTime correctedClockOutTime;
    private String correctionReason;
    private Boolean timeCorrectionApproved;
}