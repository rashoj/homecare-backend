package com.homecare.ai.context;

import com.homecare.ai.intent.AiIntentResult;
import com.homecare.dto.AiCopilotCardDTO;
import com.homecare.dto.AiCopilotDetailDTO;
import com.homecare.dto.AiCopilotResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SafeAiContextBuilder {

    private static final int MAX_METRICS = 10;
    private static final int MAX_RECORDS = 5;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final int MAX_METADATA_VALUE_LENGTH = 200;

    private static final Set<String> ALLOWED_METADATA_KEYS = Set.of(
            "Age",
            "Mobility",
            "Open EVV Issues",
            "High Severity EVV",
            "Active Incidents",
            "High Risk Incidents",
            "Role",
            "Start",
            "End",
            "Shift Type",
            "Required Skills",
            "EVV Required",
            "Billable",
            "Expires",
            "State Reportable",
            "Incident Date",
            "Appointment",
            "Created"
    );

    public AiContextDTO build(
            AiIntentResult intentResult,
            AiCopilotResponse response
    ) {
        if (intentResult == null) {
            throw new IllegalArgumentException(
                    "Intent result is required."
            );
        }

        if (response == null) {
            throw new IllegalArgumentException(
                    "Copilot response is required."
            );
        }

        List<AiContextMetricDTO> metrics =
                buildMetrics(response.getCards());

        List<AiContextRecordDTO> records =
                buildRecords(response.getDetails());

        return new AiContextDTO(
                intentResult.intent().name(),
                normalizeValue(response.getCategory(), "GENERAL"),
                normalizeValue(response.getSeverity(), "NORMAL"),
                metrics,
                records
        );
    }

    private List<AiContextMetricDTO> buildMetrics(
            List<AiCopilotCardDTO> cards
    ) {
        if (cards == null || cards.isEmpty()) {
            return List.of();
        }

        return cards.stream()
                .limit(MAX_METRICS)
                .map(card ->
                        new AiContextMetricDTO(
                                normalizeValue(
                                        card.getTitle(),
                                        "Operational Metric"
                                ),
                                normalizeValue(
                                        card.getValue(),
                                        "0"
                                ),
                                sanitizeText(
                                        card.getSubtitle(),
                                        MAX_METADATA_VALUE_LENGTH
                                )
                        )
                )
                .toList();
    }

    private List<AiContextRecordDTO> buildRecords(
            List<AiCopilotDetailDTO> details
    ) {
        if (details == null || details.isEmpty()) {
            return List.of();
        }

        AtomicInteger recordCounter =
                new AtomicInteger(1);

        List<AiContextRecordDTO> records =
                new ArrayList<>();

        details.stream()
                .limit(MAX_RECORDS)
                .forEach(detail -> {
                    int index =
                            recordCounter.getAndIncrement();

                    String category =
                            normalizeValue(
                                    detail.getCategory(),
                                    "OPERATIONAL"
                            );

                    records.add(
                            new AiContextRecordDTO(
                                    buildRecordReference(
                                            category,
                                            index
                                    ),
                                    category,
                                    sanitizeText(
                                            detail.getSubtitle(),
                                            MAX_METADATA_VALUE_LENGTH
                                    ),
                                    normalizeValue(
                                            detail.getSeverity(),
                                            "NORMAL"
                                    ),
                                    normalizeValue(
                                            detail.getStatus(),
                                            "UNKNOWN"
                                    ),
                                    sanitizeText(
                                            detail.getDescription(),
                                            MAX_DESCRIPTION_LENGTH
                                    ),
                                    sanitizeMetadata(
                                            detail.getMetadata()
                                    )
                            )
                    );
                });

        return List.copyOf(records);
    }

    private Map<String, String> sanitizeMetadata(
            Map<String, String> metadata
    ) {
        if (metadata == null || metadata.isEmpty()) {
            return Map.of();
        }

        Map<String, String> safeMetadata =
                new LinkedHashMap<>();

        metadata.forEach((key, value) -> {
            if (
                    key != null
                            && ALLOWED_METADATA_KEYS.contains(key)
            ) {
                safeMetadata.put(
                        key,
                        sanitizeText(
                                value,
                                MAX_METADATA_VALUE_LENGTH
                        )
                );
            }
        });

        return Map.copyOf(safeMetadata);
    }

    private String buildRecordReference(
            String category,
            int index
    ) {
        String normalizedCategory =
                category
                        .replaceAll("[^A-Za-z0-9]+", "_")
                        .replaceAll("^_+|_+$", "")
                        .toUpperCase();

        if (normalizedCategory.isBlank()) {
            normalizedCategory = "OPERATIONAL";
        }

        return normalizedCategory
                + "_RECORD_"
                + index;
    }

    private String sanitizeText(
            String value,
            int maxLength
    ) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String sanitized = value
                .replaceAll("[\\r\\n\\t]+", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (sanitized.length() <= maxLength) {
            return sanitized;
        }

        return sanitized.substring(0, maxLength);
    }

    private String normalizeValue(
            String value,
            String defaultValue
    ) {
        return value == null || value.isBlank()
                ? defaultValue
                : value.trim();
    }
}