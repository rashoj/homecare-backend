package com.homecare.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenAIClientService {

    private final RestClient restClient;
    private final String model;

    public OpenAIClientService(
            @Value("${openai.api.key}") String apiKey,
            @Value("${openai.model}") String model
    ) {
        this.model = model;

        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String generateExecutiveBrief(String prompt) {
        Map<String, Object> request = Map.of(
                "model", model,
                "input", List.of(
                        Map.of(
                                "role", "system",
                                "content", "You are CareBridge Copilot, a concise healthcare operations assistant. Use only the provided operational data. Do not invent facts."
                        ),
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                )
        );

        Map response = restClient.post()
                .uri("/responses")
                .body(request)
                .retrieve()
                .body(Map.class);

        return extractText(response);
    }

    private String extractText(Map response) {
        Object outputText = response.get("output_text");

        if (outputText != null) {
            return outputText.toString();
        }

        return "AI summary could not be generated.";
    }
}