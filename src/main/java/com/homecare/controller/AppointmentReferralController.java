package com.homecare.controller;

import com.homecare.dto.AppointmentReferralRequest;
import com.homecare.dto.AppointmentReferralResponse;
import com.homecare.dto.AppointmentReferralReviewRequest;
import com.homecare.service.AppointmentReferralService;
import org.springframework.web.bind.annotation.*;
import com.homecare.dto.AppointmentReferralConvertRequest;

import java.util.List;

@RestController
@RequestMapping("/api/appointment-referrals")
@CrossOrigin("*")
public class AppointmentReferralController {

    private final AppointmentReferralService referralService;

    public AppointmentReferralController(AppointmentReferralService referralService) {
        this.referralService = referralService;
    }

    @PostMapping
    public AppointmentReferralResponse createReferral(
            @RequestBody AppointmentReferralRequest request
    ) {
        return referralService.createReferral(request);
    }

    @GetMapping
    public List<AppointmentReferralResponse> getAllReferrals() {
        return referralService.getAllReferrals();
    }

    @GetMapping("/status/{status}")
    public List<AppointmentReferralResponse> getReferralsByStatus(
            @PathVariable String status
    ) {
        return referralService.getReferralsByStatus(status);
    }

    @GetMapping("/caregiver/{caregiverId}")
    public List<AppointmentReferralResponse> getReferralsByCaregiver(
            @PathVariable Long caregiverId
    ) {
        return referralService.getReferralsByCaregiver(caregiverId);
    }

    @PutMapping("/{id}/review")
    public AppointmentReferralResponse reviewReferral(
            @PathVariable Long id,
            @RequestBody AppointmentReferralReviewRequest request
    ) {
        return referralService.reviewReferral(id, request);
    }
    @PostMapping("/{id}/convert")
    public AppointmentReferralResponse convertReferralToAppointment(
            @PathVariable Long id,
            @RequestBody AppointmentReferralConvertRequest request
    ) {
        return referralService.convertReferralToAppointment(id, request);
    }
    @GetMapping("/client/{clientId}")
    public List<AppointmentReferralResponse> getReferralsByClient(
            @PathVariable Long clientId
    ) {
        return referralService.getReferralsByClient(clientId);
    }
}