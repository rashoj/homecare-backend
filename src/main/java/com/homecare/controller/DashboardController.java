package com.homecare.controller;

import com.homecare.dto.AdminDashboardResponse;
import com.homecare.dto.CaregiverDashboardResponse;
import com.homecare.dto.DashboardBreakdownResponse;
import com.homecare.dto.DashboardTrendResponse;
import com.homecare.dto.RecentActivityResponse;
import com.homecare.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    public AdminDashboardResponse getAdminDashboard(Authentication authentication) {
        return dashboardService.getAdminDashboard(authentication.getName());
    }

    @GetMapping("/caregiver/{caregiverId}")
    public CaregiverDashboardResponse getCaregiverDashboard(
            @PathVariable Long caregiverId
    ) {
        return dashboardService.getCaregiverDashboard(caregiverId);
    }

    @GetMapping("/admin/visit-trends")
    public List<DashboardTrendResponse> getVisitTrends(Authentication authentication) {
        return dashboardService.getVisitTrends(authentication.getName());
    }

    @GetMapping("/admin/mar-trends")
    public List<DashboardTrendResponse> getMarTrends(Authentication authentication) {
        return dashboardService.getMarTrends(authentication.getName());
    }

    @GetMapping("/admin/incident-severity")
    public List<DashboardBreakdownResponse> getIncidentSeverity(Authentication authentication) {
        return dashboardService.getIncidentSeverity(authentication.getName());
    }

    @GetMapping("/admin/evv-trends")
    public List<DashboardTrendResponse> getEVVTrends(Authentication authentication) {
        return dashboardService.getEVVTrends(authentication.getName());
    }

    @GetMapping("/admin/recent-activity")
    public List<RecentActivityResponse> getRecentActivity(Authentication authentication) {
        return dashboardService.getRecentActivity(authentication.getName());
    }
}