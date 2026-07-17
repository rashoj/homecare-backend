package com.homecare.ai.projection;

public interface CaregiverRiskProjection {

    Long getCaregiverId();

    Long getOpenEvvIssues();

    Long getHighSeverityEvvIssues();
}