package com.homecare.ai.intent;

public record AiIntentResult(

        AiIntent intent,

        String originalMessage,

        String normalizedMessage,

        double confidence

) {
}