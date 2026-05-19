package com.homecare.controller;

import com.homecare.dto.CaregiverAssignmentResponse;
import com.homecare.service.CaregiverAssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/caregiver/assignments")
@CrossOrigin(origins = "http://localhost:5173")
public class CaregiverAssignmentController {

    private final CaregiverAssignmentService caregiverAssignmentService;

    public CaregiverAssignmentController(CaregiverAssignmentService caregiverAssignmentService) {
        this.caregiverAssignmentService = caregiverAssignmentService;
    }

    @GetMapping("/today")
    public ResponseEntity<CaregiverAssignmentResponse> getTodayAssignment(
            @RequestParam Long caregiverId
    ) {
        return ResponseEntity.ok(
                caregiverAssignmentService.getTodayAssignment(caregiverId)
        );
    }
}