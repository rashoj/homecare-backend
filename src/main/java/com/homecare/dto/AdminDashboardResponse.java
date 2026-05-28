package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AdminDashboardResponse {

    private Long totalUsers;

    private Long totalCaregivers;

    private Long totalClients;

    private Long totalAppointments;

    private Long completedAppointments;

    private Long pendingDocuments;

    private Long totalMedications;

    private Long totalVisitNotes;

    private Long totalClockRecords;

    // NEW DEMO METRICS

    private Long missedAppointments;

    private Long openIncidents;

    private Long pendingServiceDocumentation;

    private Double marComplianceRate;
}