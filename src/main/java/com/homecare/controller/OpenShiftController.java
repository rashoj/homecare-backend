package com.homecare.controller;

import com.homecare.dto.OpenShiftRequest;
import com.homecare.dto.OpenShiftResponse;
import com.homecare.service.OpenShiftService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/open-shifts")
@CrossOrigin("*")
public class OpenShiftController {

    private final OpenShiftService openShiftService;

    public OpenShiftController(OpenShiftService openShiftService) {
        this.openShiftService = openShiftService;
    }

    @PostMapping
    public OpenShiftResponse createOpenShift(
            @RequestBody OpenShiftRequest request,
            Authentication authentication
    ) {
        return openShiftService.createOpenShift(request, authentication.getName());
    }

    @GetMapping
    public List<OpenShiftResponse> getAllOpenShifts(Authentication authentication) {
        return openShiftService.getAllOpenShifts(authentication.getName());
    }

    @GetMapping("/open")
    public List<OpenShiftResponse> getOpenShifts(Authentication authentication) {
        return openShiftService.getOpenShifts(authentication.getName());
    }
    @PostMapping("/{id}/claim")
    public OpenShiftResponse claimOpenShift(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return openShiftService.claimOpenShift(id, authentication.getName());
    }


    @PutMapping("/{id}/cancel")
    public OpenShiftResponse cancelOpenShift(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return openShiftService.cancelOpenShift(id, authentication.getName());
    }
}