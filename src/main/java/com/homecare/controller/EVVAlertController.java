package com.homecare.controller;

import com.homecare.entity.EVVAlert;
import com.homecare.service.EVVAlertService;
import org.springframework.web.bind.annotation.*;

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
    public List<EVVAlert> getAllAlerts() {
        return evvAlertService.getAllAlerts();
    }

    @GetMapping("/unread")
    public List<EVVAlert> getUnreadAlerts() {
        return evvAlertService.getUnreadAlerts();
    }

    @PutMapping("/{id}/read")
    public EVVAlert markAsRead(@PathVariable Long id) {
        return evvAlertService.markAsRead(id);
    }
}