package com.homecare.controller;

import com.homecare.dto.CaregiverTimeEntryRequest;
import com.homecare.dto.CaregiverTimeEntryResponse;
import com.homecare.service.CaregiverTimeEntryService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/caregiver-time")
@CrossOrigin("*")
public class CaregiverTimeEntryController {

    private final CaregiverTimeEntryService caregiverTimeEntryService;

    public CaregiverTimeEntryController(
            CaregiverTimeEntryService caregiverTimeEntryService
    ) {
        this.caregiverTimeEntryService = caregiverTimeEntryService;
    }

    @PostMapping("/clock-in")
    public CaregiverTimeEntryResponse clockIn(
            @RequestBody CaregiverTimeEntryRequest request,
            Authentication authentication
    ) {
        return caregiverTimeEntryService.clockIn(request, authentication.getName());
    }

    @PutMapping("/clock-out")
    public CaregiverTimeEntryResponse clockOut(
            @RequestBody CaregiverTimeEntryRequest request,
            Authentication authentication
    ) {
        return caregiverTimeEntryService.clockOut(request, authentication.getName());
    }

    @GetMapping("/current")
    public CaregiverTimeEntryResponse getCurrentStatus(Authentication authentication) {
        return caregiverTimeEntryService.getCurrentStatus(authentication.getName());
    }

    @GetMapping("/me")
    public List<CaregiverTimeEntryResponse> getMyTimeEntries(Authentication authentication) {
        return caregiverTimeEntryService.getMyTimeEntries(authentication.getName());
    }

    @GetMapping("/organization")
    public List<CaregiverTimeEntryResponse> getOrganizationTimeEntries(
            Authentication authentication
    ) {
        return caregiverTimeEntryService.getOrganizationTimeEntries(authentication.getName());
    }
}