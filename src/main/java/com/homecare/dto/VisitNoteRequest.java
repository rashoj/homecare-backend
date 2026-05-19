package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VisitNoteRequest {

    private Long appointmentId;

    private String generalNotes;

    private String meals;

    private String medicationNotes;

    private String mobilityNotes;

    private String moodNotes;

    private String hygieneCare;

    private String safetyConcerns;

    private String familyUpdate;

    private Boolean incidentReported;

    private String incidentDetails;
}