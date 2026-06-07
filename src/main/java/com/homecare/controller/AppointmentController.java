package com.homecare.controller;

import com.homecare.dto.AppointmentRequest;
import com.homecare.dto.AppointmentResponse;
import com.homecare.dto.AppointmentStatusUpdateRequest;
import com.homecare.service.AppointmentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin("*")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public AppointmentResponse createAppointment(
            @RequestBody AppointmentRequest request,
            Authentication authentication
    ) {
        return appointmentService.createAppointment(request, authentication.getName());
    }

    @GetMapping
    public List<AppointmentResponse> getAllAppointments(Authentication authentication) {
        return appointmentService.getAllAppointments(authentication.getName());
    }

    @GetMapping("/client/{clientId}")
    public List<AppointmentResponse> getAppointmentsByClient(
            @PathVariable Long clientId,
            Authentication authentication
    ) {
        return appointmentService.getAppointmentsByClient(clientId, authentication.getName());
    }

    @PutMapping("/{id}/status")
    public AppointmentResponse updateAppointmentStatus(
            @PathVariable Long id,
            @RequestBody AppointmentStatusUpdateRequest request,
            Authentication authentication
    ) {
        return appointmentService.updateAppointmentStatus(id, request, authentication.getName());
    }

    @GetMapping("/caregiver/{caregiverId}")
    public List<AppointmentResponse> getAppointmentsByCaregiver(
            @PathVariable Long caregiverId,
            Authentication authentication
    ) {
        return appointmentService.getAppointmentsByCaregiver(caregiverId, authentication.getName());
    }

    @GetMapping("/{id}")
    public AppointmentResponse getAppointmentById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return appointmentService.getAppointmentById(id, authentication.getName());
    }
}