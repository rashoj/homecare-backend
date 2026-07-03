package com.homecare.dto;

public class AiCopilotCardDTO {

    private String title;
    private String value;
    private String subtitle;
    private String tone;

    public AiCopilotCardDTO(String title, String value, String subtitle, String tone) {
        this.title = title;
        this.value = value;
        this.subtitle = subtitle;
        this.tone = tone;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getTone() {
        return tone;
    }
}