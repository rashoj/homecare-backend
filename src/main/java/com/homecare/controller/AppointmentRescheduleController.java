package com.homecare.controller;

import com.homecare.dto.AppointmentRescheduleRequestDto;
import com.homecare.dto.AppointmentRescheduleResponse;
import com.homecare.dto.AppointmentRescheduleReviewRequest;
import com.homecare.service.AppointmentRescheduleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointment-reschedule-requests")
@CrossOrigin("*")
public class AppointmentRescheduleController {

    private final AppointmentRescheduleService rescheduleService;

    public AppointmentRescheduleController(
            AppointmentRescheduleService rescheduleService
    ) {
        this.rescheduleService = rescheduleService;
    }

    @PostMapping
    public AppointmentRescheduleResponse createRequest(
            @RequestBody AppointmentRescheduleRequestDto request
    ) {
        return rescheduleService.createRequest(request);
    }

    @GetMapping
    public List<AppointmentRescheduleResponse> getAllRequests() {
        return rescheduleService.getAllRequests();
    }

    @GetMapping("/client/{clientId}")
    public List<AppointmentRescheduleResponse> getRequestsByClient(
            @PathVariable Long clientId
    ) {
        return rescheduleService.getRequestsByClient(clientId);
    }

    @GetMapping("/caregiver/{caregiverId}")
    public List<AppointmentRescheduleResponse> getRequestsByCaregiver(
            @PathVariable Long caregiverId
    ) {
        return rescheduleService.getRequestsByCaregiver(caregiverId);
    }

    @GetMapping("/appointment/{appointmentId}")
    public List<AppointmentRescheduleResponse> getRequestsByAppointment(
            @PathVariable Long appointmentId
    ) {
        return rescheduleService.getRequestsByAppointment(appointmentId);
    }

    @PutMapping("/{id}/review")
    public AppointmentRescheduleResponse reviewRequest(
            @PathVariable Long id,
            @RequestBody AppointmentRescheduleReviewRequest request
    ) {
        return rescheduleService.reviewRequest(id, request);
    }
}