package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IncidentReviewRequest {

    private Long reviewedByUserId;

    private String reviewStatus;

    private String supervisorNotes;

    private String correctiveAction;

    private String followUpRequired;


}