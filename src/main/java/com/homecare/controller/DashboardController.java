package com.homecare.controller;

import com.homecare.dto.AdminDashboardResponse;
import com.homecare.dto.CaregiverDashboardResponse;
import com.homecare.service.DashboardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin("*")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin")
    public AdminDashboardResponse getAdminDashboard() {
        return dashboardService.getAdminDashboard();
    }

    @GetMapping("/caregiver/{caregiverId}")
    public CaregiverDashboardResponse getCaregiverDashboard(
            @PathVariable Long caregiverId
    ) {
        return dashboardService.getCaregiverDashboard(caregiverId);
    }
}