package com.homecare.ai.dto;

public record EVVInsightDTO(
        long openExceptions,
        long reviewedExceptions,
        long resolvedExceptions,
        long highSeverityExceptions,
        long unreadAlerts
) {}