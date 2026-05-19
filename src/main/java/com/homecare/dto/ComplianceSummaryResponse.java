package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ComplianceSummaryResponse {

    private long missedMedications;

    private long visitsMissingNotes;

    private long openClockRecords;

    private long incidentsReported;
}