package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ClientRiskRowResponse {

    private Long clientId;
    private String clientName;

    private long totalIncidents;
    private long highSeverityIncidents;
    private long criticalIncidents;
    private long stateReportableIncidents;

    private long missedMedications;
    private long visitsMissingNotes;
    private long openClockRecords;

    private long riskScore;
    private String riskLevel;
}