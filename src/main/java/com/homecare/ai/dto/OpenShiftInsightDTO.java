package com.homecare.ai.dto;

public record OpenShiftInsightDTO(
        long openShifts,
        long claimedShifts,
        long assignedShifts
) {}