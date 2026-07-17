package com.homecare.ai.projection;

public interface ClientRiskProjection {

    Long getClientId();

    Long getOpenEvvIssues();

    Long getHighSeverityEvvIssues();

    Long getActiveIncidents();

    Long getHighRiskIncidents();
}