package com.homecare.controller;

import com.homecare.dto.*;
import com.homecare.service.ISPService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/isp")
@CrossOrigin("*")
public class ISPController {

    private final ISPService ispService;

    public ISPController(ISPService ispService) {
        this.ispService = ispService;
    }

    @PostMapping("/plans")
    public ISPPlanResponse createPlan(@RequestBody ISPPlanRequest request) {
        return ispService.createPlan(request);
    }

    @GetMapping("/clients/{clientId}/plans")
    public List<ISPPlanResponse> getPlansByClient(@PathVariable Long clientId) {
        return ispService.getPlansByClient(clientId);
    }

    @PostMapping("/goals")
    public ISPGoalResponse createGoal(@RequestBody ISPGoalRequest request) {
        return ispService.createGoal(request);
    }

    @GetMapping("/clients/{clientId}/goals/active")
    public List<ISPGoalResponse> getActiveGoalsByClient(@PathVariable Long clientId) {
        return ispService.getActiveGoalsByClient(clientId);
    }

    @PostMapping("/progress")
    public ISPGoalProgressResponse submitProgress(@RequestBody ISPGoalProgressRequest request) {
        return ispService.submitProgress(request);
    }

    @GetMapping("/clients/{clientId}/progress")
    public List<ISPGoalProgressResponse> getProgressByClient(@PathVariable Long clientId) {
        return ispService.getProgressByClient(clientId);
    }

    @GetMapping("/service-documentation/{documentationId}/progress")
    public List<ISPGoalProgressResponse> getProgressByDocumentation(
            @PathVariable Long documentationId
    ) {
        return ispService.getProgressByDocumentation(documentationId);
    }
}