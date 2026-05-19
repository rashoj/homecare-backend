package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ClientComplianceRowResponse {

    private Long clientId;

    private String clientName;

    private long missedMedications;

    private long refusedMedications;

    private long visitsMissingNotes;

    private long openClockRecords;

    private long incidentsReported;

    private String complianceStatus;
}