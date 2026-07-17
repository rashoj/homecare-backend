package com.homecare.dto;

import java.util.Map;

public class AiCopilotDetailDTO {

    private String title;
    private String subtitle;
    private String description;
    private String category;
    private String severity;
    private String status;
    private String route;
    private Map<String, String> metadata;

    public AiCopilotDetailDTO(
            String title,
            String subtitle,
            String description,
            String category,
            String severity,
            String status,
            String route,
            Map<String, String> metadata
    ) {
        this.title = title;
        this.subtitle = subtitle;
        this.description = description;
        this.category = category;
        this.severity = severity;
        this.status = status;
        this.route = route;
        this.metadata = metadata;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getSeverity() {
        return severity;
    }

    public String getStatus() {
        return status;
    }

    public String getRoute() {
        return route;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
}