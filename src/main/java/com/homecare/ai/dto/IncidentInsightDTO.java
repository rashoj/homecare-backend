package com.homecare.ai.dto;

public record IncidentInsightDTO(
        long submittedIncidents,
        long underReviewIncidents,
        long highRiskIncidents,
        long stateReportableIncidents
) {}