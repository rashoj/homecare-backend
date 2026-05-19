package com.homecare.controller;

import com.homecare.dto.AiSummaryRequest;
import com.homecare.service.AiSummaryService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiSummaryService aiSummaryService;

    public AiController(AiSummaryService aiSummaryService) {
        this.aiSummaryService = aiSummaryService;
    }

    @PostMapping("/visit-summary")
    public String generateSummary(
            @RequestBody AiSummaryRequest request
    ) {

        return aiSummaryService.generateVisitSummary(
                request.getVisitNote()
        );
    }
}