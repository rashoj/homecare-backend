package com.homecare.controller;

import com.homecare.dto.SchedulerCaregiverResponse;
import com.homecare.service.SchedulerService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduler")
@CrossOrigin("*")
public class SchedulerController {

    private final SchedulerService schedulerService;

    public SchedulerController(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @GetMapping("/caregivers")
    public List<SchedulerCaregiverResponse> getSchedulerCaregivers(
            Authentication authentication
    ) {
        return schedulerService.getSchedulerCaregivers(authentication.getName());
    }
}