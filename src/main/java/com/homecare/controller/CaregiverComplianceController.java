package com.homecare.controller;

import com.homecare.dto.CaregiverComplianceRequest;
import com.homecare.dto.CaregiverComplianceResponse;
import com.homecare.service.CaregiverComplianceService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/caregiver-compliance")
@CrossOrigin("*")
public class CaregiverComplianceController {

    private final CaregiverComplianceService service;

    public CaregiverComplianceController(CaregiverComplianceService service) {
        this.service = service;
    }

    @PostMapping
    public CaregiverComplianceResponse create(
            @RequestBody CaregiverComplianceRequest request,
            Authentication authentication
    ) {
        return service.create(request, authentication.getName());
    }

    @GetMapping("/caregiver/{caregiverId}")
    public List<CaregiverComplianceResponse> getByCaregiver(
            @PathVariable Long caregiverId,
            Authentication authentication
    ) {
        return service.getByCaregiver(caregiverId, authentication.getName());
    }

    @GetMapping("/expiring")
    public List<CaregiverComplianceResponse> getExpiredOrExpiring(
            @RequestParam(defaultValue = "30") int days,
            Authentication authentication
    ) {
        return service.getExpiredOrExpiring(authentication.getName(), days);
    }
}