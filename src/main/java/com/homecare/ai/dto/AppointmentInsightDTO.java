package com.homecare.ai.dto;

public record AppointmentInsightDTO(
        long totalToday,
        long scheduledToday,
        long completedToday,
        long unassignedToday
) {}