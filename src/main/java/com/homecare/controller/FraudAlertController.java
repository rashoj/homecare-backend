package com.homecare.controller;

import com.homecare.dto.FraudAlertResponse;
import com.homecare.service.FraudAlertService;
import com.homecare.service.FraudDetectionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.homecare.dto.FraudSummaryResponse;

import java.util.List;

@RestController
@RequestMapping("/api/fraud-alerts")
@CrossOrigin("*")
public class FraudAlertController {

    private final FraudAlertService fraudAlertService;
    private final FraudDetectionService fraudDetectionService;

    public FraudAlertController(
            FraudAlertService fraudAlertService,
            FraudDetectionService fraudDetectionService
    ) {
        this.fraudAlertService = fraudAlertService;
        this.fraudDetectionService = fraudDetectionService;
    }

    @GetMapping
    public List<FraudAlertResponse> getAlerts(Authentication authentication) {
        return fraudAlertService.getAlerts(authentication.getName());
    }

    @GetMapping("/open")
    public List<FraudAlertResponse> getOpenAlerts(Authentication authentication) {
        return fraudAlertService.getOpenAlerts(authentication.getName());
    }

    @PutMapping("/{id}/resolve")
    public FraudAlertResponse resolveAlert(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return fraudAlertService.resolveAlert(id, authentication.getName());
    }

    @PostMapping("/run-detection")
    public String runDetection() {
        fraudDetectionService.detectMissingClockOuts();
        return "Fraud detection completed.";
    }

    @GetMapping("/summary")
    public FraudSummaryResponse getSummary(
            Authentication authentication
    ) {
        return fraudAlertService.getSummary(
                authentication.getName()
        );
    }
}