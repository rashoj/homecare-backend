package com.homecare.controller;

import com.homecare.dto.ClientComplianceRowResponse;
import com.homecare.dto.ComplianceSummaryResponse;
import com.homecare.dto.MissedMedicationAlertResponse;
import com.homecare.dto.MissingVisitNoteAlertResponse;
import com.homecare.service.ComplianceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compliance")
@CrossOrigin("*")
public class ComplianceController {

    private final ComplianceService complianceService;

    public ComplianceController(ComplianceService complianceService) {
        this.complianceService = complianceService;
    }

    @GetMapping("/summary")
    public ComplianceSummaryResponse getSummary() {
        return complianceService.getSummary();
    }

    @GetMapping("/clients")
    public List<ClientComplianceRowResponse> getClientComplianceRows() {
        return complianceService.getClientComplianceRows();
    }

    @GetMapping("/missed-medications")
    public List<MissedMedicationAlertResponse> getMissedMedicationAlerts() {
        return complianceService.getMissedMedicationAlerts();
    }
    @GetMapping("/missing-visit-notes")
    public List<MissingVisitNoteAlertResponse> getMissingVisitNoteAlerts() {
        return complianceService.getMissingVisitNoteAlerts();
    }
}