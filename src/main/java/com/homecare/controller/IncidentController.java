package com.homecare.controller;

import com.homecare.dto.IncidentRequest;
import com.homecare.dto.IncidentResponse;
import com.homecare.dto.IncidentReviewRequest;
import com.homecare.service.IncidentService;
import org.springframework.web.bind.annotation.*;
import com.homecare.service.IncidentPdfService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@CrossOrigin("*")
public class IncidentController {

    private final IncidentService incidentService;
    private final IncidentPdfService incidentPdfService;
    public IncidentController(
            IncidentService incidentService,
            IncidentPdfService incidentPdfService
    ) {
        this.incidentService = incidentService;
        this.incidentPdfService = incidentPdfService;
    }


    @PostMapping
    public IncidentResponse createIncident(@RequestBody IncidentRequest request) {
        return incidentService.createIncident(request);
    }

    @GetMapping
    public List<IncidentResponse> getAllIncidents() {
        return incidentService.getAllIncidents();
    }

    @GetMapping("/{id}")
    public IncidentResponse getIncidentById(@PathVariable Long id) {
        return incidentService.getIncidentById(id);
    }

    @GetMapping("/client/{clientId}")
    public List<IncidentResponse> getIncidentsByClient(@PathVariable Long clientId) {
        return incidentService.getIncidentsByClient(clientId);
    }

    @GetMapping("/caregiver/{caregiverId}")
    public List<IncidentResponse> getIncidentsByCaregiver(@PathVariable Long caregiverId) {
        return incidentService.getIncidentsByCaregiver(caregiverId);
    }

    @PutMapping("/{id}/review")
    public IncidentResponse reviewIncident(
            @PathVariable Long id,
            @RequestBody IncidentReviewRequest request
    ) {
        return incidentService.reviewIncident(id, request);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadIncidentPdf(@PathVariable Long id) {
        byte[] pdf = incidentPdfService.generateIncidentPdf(id);

        return ResponseEntity.ok()
                .header(
                        "Content-Disposition",
                        "attachment; filename=incident-report-" + id + ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}