package com.homecare.ai.context;

import java.util.Map;

public record AiContextRecordDTO(
        String recordReference,
        String category,
        String type,
        String severity,
        String status,
        String description,
        Map<String, String> metadata
) {
}