package com.homecare.ai.intent;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class CaregiverNameExtractor {

    private static final List<String> PHRASES_TO_REMOVE = List.of(
            "show me",
            "show",
            "tell me about",
            "tell me",
            "find",
            "lookup",
            "look up",
            "caregiver",
            "employee",
            "worker",
            "staff member",
            "information",
            "details",
            "detail"
    );

    public String extract(String message) {
        if (message == null || message.isBlank()) {
            return "";
        }

        String normalized = message
                .trim()
                .toLowerCase(Locale.ROOT);

        for (String phrase : PHRASES_TO_REMOVE) {
            normalized = normalized.replace(phrase, " ");
        }

        return normalized
                .replaceAll("[^a-z0-9'\\- ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}