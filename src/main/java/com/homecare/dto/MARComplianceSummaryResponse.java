package com.homecare.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MARComplianceSummaryResponse {
    private long totalMARLogs;
    private long administeredCount;
    private long missedCount;
    private long refusedCount;
    private long heldCount;
    private long prnGivenCount;
    private long overdueCount;
    private double marComplianceRate;
}