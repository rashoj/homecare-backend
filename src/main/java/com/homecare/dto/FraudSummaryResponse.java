package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FraudSummaryResponse {

    private long openAlerts;
    private long highAlerts;
    private long criticalAlerts;
    private int totalRiskScore;
}