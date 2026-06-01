package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class FamilyVisitNoteResponse {

    private Long id;
    private Long appointmentId;

    private String caregiverName;

    private String generalNotes;
    private String meals;
    private String medicationNotes;
    private String mobilityNotes;
    private String moodNotes;
    private String hygieneCare;
    private String safetyConcerns;
    private String familyUpdate;
    private String aiSummary;

    private LocalDateTime createdAt;
}