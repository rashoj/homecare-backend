package com.homecare.controller;

import com.homecare.entity.EVVAlert;
import com.homecare.service.EVVAlertService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.homecare.dto.EVVAlertResponse;

import java.util.List;

@RestController
@RequestMapping("/api/evv-alerts")
@CrossOrigin("*")
public class EVVAlertController {

    private final EVVAlertService evvAlertService;

    public EVVAlertController(EVVAlertService evvAlertService) {
        this.evvAlertService = evvAlertService;
    }

    @GetMapping
    public List<EVVAlertResponse> getAllAlerts(Authentication authentication) {
        return evvAlertService.getAllAlerts(authentication.getName());
    }

    @GetMapping("/unread")
    public List<EVVAlertResponse> getUnreadAlerts(Authentication authentication) {
        return evvAlertService.getUnreadAlerts(authentication.getName());
    }

    @PutMapping("/{id}/read")
    public EVVAlertResponse markAsRead(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return evvAlertService.markAsRead(
                id,
                authentication.getName()
        );
    }
}