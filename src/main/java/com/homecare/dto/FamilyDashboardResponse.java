package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FamilyDashboardResponse {

    private Long familyUserId;
    private String familyName;

    private Long clientId;
    private String clientName;

    private Long upcomingAppointments;
    private Long completedVisits;
    private Long recentVisitNotes;
    private Long sharedDocuments;
    private Long activeMedications;
}