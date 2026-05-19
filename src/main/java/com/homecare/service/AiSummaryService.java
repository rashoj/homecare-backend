package com.homecare.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class AiSummaryService {

    private final RestClient restClient;

    public AiSummaryService(@Value("${openai.api.key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String generateVisitSummary(String visitNoteText) {

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", "Generate a concise professional healthcare visit summary. Do not include placeholders, signatures, dates, headings, or contact information."
                            ),
                            Map.of(
                                    "role", "user",
                                    "content", "Create a professional visit summary from this caregiver note:\n\n" + visitNoteText
                            )
                    ),
                    "temperature", 0.4,
                    "max_tokens", 200
            );

            Map response = restClient.post()
                    .uri("/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            System.out.println("OpenAI response: " + response);

            if (response == null || response.get("choices") == null) {
                throw new RuntimeException("OpenAI response was empty");
            }

            List choices = (List) response.get("choices");

            if (choices.isEmpty()) {
                throw new RuntimeException("OpenAI response choices were empty");
            }

            Map firstChoice = (Map) choices.get(0);
            Map message = (Map) firstChoice.get("message");

            if (message == null || message.get("content") == null) {
                throw new RuntimeException("OpenAI message content was empty");
            }

            return message.get("content").toString();

        } catch (Exception error) {
            System.out.println("AI summary generation failed: " + error.getMessage());

            return "AI summary could not be generated for this visit note.";
        }
    }}