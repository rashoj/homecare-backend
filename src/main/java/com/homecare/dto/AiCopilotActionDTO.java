package com.homecare.dto;

public class AiCopilotActionDTO {

    private String label;
    private String route;

    public AiCopilotActionDTO(String label, String route) {
        this.label = label;
        this.route = route;
    }

    public String getLabel() {
        return label;
    }

    public String getRoute() {
        return route;
    }
}