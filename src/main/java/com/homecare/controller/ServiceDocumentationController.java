package com.homecare.controller;

import com.homecare.dto.ServiceDocumentationRequest;
import com.homecare.dto.ServiceDocumentationResponse;
import com.homecare.dto.ServiceDocumentationReviewRequest;
import com.homecare.entity.ServiceDocumentationAuditLog;
import com.homecare.service.ServiceDocumentationPdfService;
import com.homecare.service.ServiceDocumentationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-documentation")
@CrossOrigin("*")
public class ServiceDocumentationController {

    private final ServiceDocumentationService serviceDocumentationService;
    private final ServiceDocumentationPdfService pdfService;

    public ServiceDocumentationController(
            ServiceDocumentationService serviceDocumentationService,
            ServiceDocumentationPdfService pdfService
    ) {
        this.serviceDocumentationService = serviceDocumentationService;
        this.pdfService = pdfService;
    }

    @PostMapping
    public ServiceDocumentationResponse submitDocumentation(
            @RequestBody ServiceDocumentationRequest request,
            Authentication authentication
    ) {
        return serviceDocumentationService.submitDocumentation(
                request,
                authentication.getName()
        );
    }

    @PutMapping("/{id}/review")
    public ServiceDocumentationResponse reviewDocumentation(
            @PathVariable Long id,
            @RequestBody ServiceDocumentationReviewRequest request,
            Authentication authentication
    ) {
        return serviceDocumentationService.reviewDocumentation(
                id,
                request,
                authentication.getName()
        );
    }

    @GetMapping("/client/{clientId}")
    public List<ServiceDocumentationResponse> getDocumentationByClient(
            @PathVariable Long clientId,
            Authentication authentication
    ) {
        return serviceDocumentationService.getDocumentationByClient(
                clientId,
                authentication.getName()
        );
    }

    @GetMapping("/pending")
    public List<ServiceDocumentationResponse> getPendingDocumentation(
            Authentication authentication
    ) {
        return serviceDocumentationService.getPendingDocumentation(
                authentication.getName()
        );
    }

    @GetMapping("/appointment/{appointmentId}")
    public ServiceDocumentationResponse getDocumentationByAppointment(
            @PathVariable Long appointmentId,
            Authentication authentication
    ) {
        return serviceDocumentationService.getDocumentationByAppointment(
                appointmentId,
                authentication.getName()
        );
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        byte[] pdf = pdfService.generatePdf(id);

        return ResponseEntity.ok()
                .header(
                        "Content-Disposition",
                        "attachment; filename=service-documentation-" + id + ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{id}/audit-logs")
    public ResponseEntity<List<ServiceDocumentationAuditLog>> getAuditLogs(@PathVariable Long id) {
        return ResponseEntity.ok(serviceDocumentationService.getAuditLogs(id));
    }
}