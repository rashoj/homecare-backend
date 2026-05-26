package com.homecare.controller;

import com.homecare.dto.AppointmentRequest;
import com.homecare.dto.AppointmentResponse;
import com.homecare.dto.AppointmentStatusUpdateRequest;
import com.homecare.service.AppointmentService;
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
    public AppointmentResponse createAppointment(@RequestBody AppointmentRequest request) {
        return appointmentService.createAppointment(request);
    }

    @GetMapping
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }
    @GetMapping("/client/{clientId}")
    public List<AppointmentResponse> getAppointmentsByClient(@PathVariable Long clientId) {
        return appointmentService.getAppointmentsByClient(clientId);
    }
    @PutMapping("/{id}/status")
    public AppointmentResponse updateAppointmentStatus(
            @PathVariable Long id,
            @RequestBody AppointmentStatusUpdateRequest request
    ) {
        return appointmentService.updateAppointmentStatus(id, request);
    }


    @GetMapping("/caregiver/{caregiverId}")
    public List<AppointmentResponse> getAppointmentsByCaregiver(
            @PathVariable Long caregiverId
    ) {

        return appointmentService.getAppointmentsByCaregiver(caregiverId);
    }
    @GetMapping("/{id}")
    public AppointmentResponse getAppointmentById(@PathVariable Long id) {
        return appointmentService.getAppointmentById(id);
    }
}
