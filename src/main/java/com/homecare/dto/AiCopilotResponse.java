package com.homecare.dto;

import java.util.List;

public class AiCopilotResponse {

    private String answer;
    private List<AiCopilotCardDTO> cards;
    private List<AiCopilotActionDTO> actions;
    private List<AiCopilotDetailDTO> details;
    private String category;
    private String severity;

    public AiCopilotResponse(
            String answer,
            List<AiCopilotCardDTO> cards,
            List<AiCopilotActionDTO> actions
    ) {
        this.answer = answer;
        this.cards = cards;
        this.actions = actions;
        this.details = List.of();
        this.category = null;
        this.severity = null;
    }

    public AiCopilotResponse(
            String answer,
            List<AiCopilotCardDTO> cards,
            List<AiCopilotActionDTO> actions,
            List<AiCopilotDetailDTO> details,
            String category,
            String severity
    ) {
        this.answer = answer;
        this.cards = cards;
        this.actions = actions;
        this.details = details;
        this.category = category;
        this.severity = severity;
    }

    public String getAnswer() {
        return answer;
    }

    public List<AiCopilotCardDTO> getCards() {
        return cards;
    }

    public List<AiCopilotActionDTO> getActions() {
        return actions;
    }

    public List<AiCopilotDetailDTO> getDetails() {
        return details;
    }

    public String getCategory() {
        return category;
    }

    public String getSeverity() {
        return severity;
    }
}