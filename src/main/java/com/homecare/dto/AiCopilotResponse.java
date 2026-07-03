package com.homecare.dto;

import java.util.List;

public class AiCopilotResponse {

    private String answer;
    private List<AiCopilotCardDTO> cards;
    private List<AiCopilotActionDTO> actions;

    public AiCopilotResponse(
            String answer,
            List<AiCopilotCardDTO> cards,
            List<AiCopilotActionDTO> actions
    ) {
        this.answer = answer;
        this.cards = cards;
        this.actions = actions;
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
}