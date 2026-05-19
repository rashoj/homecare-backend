package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReportSummaryResponse {

    private Long totalClients;
    private Long totalCaregivers;
    private Long totalAppointments;
    private Long completedVisits;
    private Double totalHours;
    private Double estimatedRevenue;
    private Double estimatedPayroll;
    private Double estimatedGrossMargin;
    private Long pendingDocuments;
}