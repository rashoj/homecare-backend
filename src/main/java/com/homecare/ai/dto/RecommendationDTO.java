package com.homecare.ai.dto;

public record RecommendationDTO(
        String title,
        String description,
        String priority,
        String route
) {}