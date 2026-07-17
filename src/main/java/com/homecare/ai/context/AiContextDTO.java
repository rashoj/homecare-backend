package com.homecare.ai.context;

import java.util.List;

public record AiContextDTO(
        String intent,
        String category,
        String overallSeverity,
        List<AiContextMetricDTO> metrics,
        List<AiContextRecordDTO> records
) {
}