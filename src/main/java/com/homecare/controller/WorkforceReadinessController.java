package com.homecare.controller;

import com.homecare.dto.WorkforceReadinessResponse;
import com.homecare.service.WorkforceReadinessService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workforce-readiness")
@CrossOrigin("*")
public class WorkforceReadinessController {

    private final WorkforceReadinessService service;

    public WorkforceReadinessController(WorkforceReadinessService service) {
        this.service = service;
    }

    @GetMapping("/caregiver/{caregiverId}")
    public WorkforceReadinessResponse getCaregiverReadiness(
            @PathVariable Long caregiverId,
            Authentication authentication
    ) {
        return service.getReadiness(caregiverId, authentication.getName());
    }
}