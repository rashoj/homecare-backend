package com.homecare.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EVVComplianceDashboardResponse {

    private long totalExceptions;
    private long openExceptions;
    private long reviewedExceptions;
    private long resolvedExceptions;
    private long highSeverityExceptions;

    private double complianceRate;

    private Map<String, Long> exceptionsByType;
    private Map<String, Long> exceptionsByClient;
    private Map<String, Long> exceptionsByCaregiver;
}