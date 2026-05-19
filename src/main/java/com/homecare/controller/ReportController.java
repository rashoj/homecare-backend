package com.homecare.controller;

import com.homecare.dto.ReportSummaryResponse;
import com.homecare.service.ReportService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin("*")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public ReportSummaryResponse getSummaryReport(
            @RequestParam Double clientRate,
            @RequestParam Double caregiverRate
    ) {
        return reportService.getSummary(clientRate, caregiverRate);
    }
}