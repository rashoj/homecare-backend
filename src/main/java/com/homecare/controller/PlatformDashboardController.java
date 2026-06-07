package com.homecare.controller;

import com.homecare.dto.PlatformDashboardResponse;
import com.homecare.service.PlatformDashboardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/platform")
@CrossOrigin("*")
public class PlatformDashboardController {

    private final PlatformDashboardService platformDashboardService;

    public PlatformDashboardController(
            PlatformDashboardService platformDashboardService
    ) {
        this.platformDashboardService = platformDashboardService;
    }

    @GetMapping("/dashboard")
    public PlatformDashboardResponse getDashboard() {
        return platformDashboardService.getDashboard();
    }
}