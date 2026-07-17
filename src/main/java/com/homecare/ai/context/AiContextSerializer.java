package com.homecare.ai.context;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AiContextSerializer {

    public String serialize(AiContextDTO context) {
        if (context == null) {
            throw new IllegalArgumentException(
                    "AI context is required."
            );
        }

        StringBuilder builder =
                new StringBuilder();

        builder.append("CAREB​RIDGE_OPERATIONAL_CONTEXT\n");
        builder.append("Intent: ")
                .append(context.intent())
                .append("\n");

        builder.append("Category: ")
                .append(context.category())
                .append("\n");

        builder.append("Overall severity: ")
                .append(context.overallSeverity())
                .append("\n\n");

        appendMetrics(builder, context);

        appendRecords(builder, context);

        return builder.toString().trim();
    }

    private void appendMetrics(
            StringBuilder builder,
            AiContextDTO context
    ) {
        builder.append("METRICS\n");

        if (
                context.metrics() == null
                        || context.metrics().isEmpty()
        ) {
            builder.append("- No metrics provided.\n\n");
            return;
        }

        for (AiContextMetricDTO metric : context.metrics()) {
            builder.append("- ")
                    .append(metric.name())
                    .append(": ")
                    .append(metric.value());

            if (
                    metric.description() != null
                            && !metric.description().isBlank()
            ) {
                builder.append(" (")
                        .append(metric.description())
                        .append(")");
            }

            builder.append("\n");
        }

        builder.append("\n");
    }

    private void appendRecords(
            StringBuilder builder,
            AiContextDTO context
    ) {
        builder.append("RECORDS\n");

        if (
                context.records() == null
                        || context.records().isEmpty()
        ) {
            builder.append("- No matching records provided.\n");
            return;
        }

        for (AiContextRecordDTO record : context.records()) {
            builder.append("\n")
                    .append(record.recordReference())
                    .append("\n");

            builder.append("Category: ")
                    .append(record.category())
                    .append("\n");

            builder.append("Type: ")
                    .append(record.type())
                    .append("\n");

            builder.append("Severity: ")
                    .append(record.severity())
                    .append("\n");

            builder.append("Status: ")
                    .append(record.status())
                    .append("\n");

            if (
                    record.description() != null
                            && !record.description().isBlank()
            ) {
                builder.append("Description: ")
                        .append(record.description())
                        .append("\n");
            }

            appendMetadata(
                    builder,
                    record.metadata()
            );
        }
    }

    private void appendMetadata(
            StringBuilder builder,
            Map<String, String> metadata
    ) {
        if (
                metadata == null
                        || metadata.isEmpty()
        ) {
            return;
        }

        builder.append("Metadata:\n");

        metadata.forEach((key, value) ->
                builder.append("- ")
                        .append(key)
                        .append(": ")
                        .append(value)
                        .append("\n")
        );
    }
}