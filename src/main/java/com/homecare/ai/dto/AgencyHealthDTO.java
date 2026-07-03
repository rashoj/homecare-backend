package com.homecare.ai.dto;

public record AgencyHealthDTO(
        int score,
        String label,
        String tone
) {}