package com.homecare.controller;

import com.homecare.dto.AdminDashboardResponse;
import com.homecare.dto.CaregiverDashboardResponse;
import com.homecare.service.DashboardService;
import org.springframework.web.bind.annotation.*;
import com.homecare.dto.DashboardTrendResponse;
import com.homecare.dto.DashboardBreakdownResponse;
import com.homecare.dto.RecentActivityResponse;

import java.util.List;

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
    @GetMapping("/admin/visit-trends")
    public List<DashboardTrendResponse> getVisitTrends() {
        return dashboardService.getVisitTrends();
    }

    @GetMapping("/admin/mar-trends")
    public List<DashboardTrendResponse> getMarTrends() {
        return dashboardService.getMarTrends();
    }

    @GetMapping("/admin/incident-severity")
    public List<DashboardBreakdownResponse> getIncidentSeverity() {
        return dashboardService.getIncidentSeverity();
    }

    @GetMapping("/admin/evv-trends")
    public List<DashboardTrendResponse> getEVVTrends() {
        return dashboardService.getEVVTrends();
    }

    @GetMapping("/admin/recent-activity")
    public List<RecentActivityResponse> getRecentActivity() {
        return dashboardService.getRecentActivity();
    }
}