package com.homecare.controller;

import com.homecare.dto.AdminOperationsDashboardResponse;
import com.homecare.service.AdminOperationsDashboardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin-operations-dashboard")
@CrossOrigin("*")
public class AdminOperationsDashboardController {

    private final AdminOperationsDashboardService dashboardService;

    public AdminOperationsDashboardController(
            AdminOperationsDashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public AdminOperationsDashboardResponse getSummary() {
        return dashboardService.getSummary();
    }
}