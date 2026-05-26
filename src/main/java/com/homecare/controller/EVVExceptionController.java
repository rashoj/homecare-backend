package com.homecare.controller;

import com.homecare.dto.EVVComplianceDashboardResponse;
import com.homecare.dto.EVVExceptionResponse;
import com.homecare.dto.EVVExceptionReviewRequest;
import com.homecare.dto.EVVExceptionSummaryResponse;
import com.homecare.entity.EVVExceptionAuditLog;
import com.homecare.service.EVVExceptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evv-exceptions")
@CrossOrigin("*")
public class EVVExceptionController {

    private final EVVExceptionService evvExceptionService;

    public EVVExceptionController(EVVExceptionService evvExceptionService) {
        this.evvExceptionService = evvExceptionService;
    }

    @GetMapping
    public List<EVVExceptionResponse> getAllExceptions() {
        return evvExceptionService.getAllExceptions();
    }

    @GetMapping("/open")
    public List<EVVExceptionResponse> getOpenExceptions() {
        return evvExceptionService.getOpenExceptions();
    }

    @GetMapping("/summary")
    public ResponseEntity<EVVExceptionSummaryResponse> getSummary() {
        return ResponseEntity.ok(evvExceptionService.getSummary());
    }

    @GetMapping("/{id}/audit-logs")
    public ResponseEntity<List<EVVExceptionAuditLog>> getAuditLogs(@PathVariable Long id) {
        return ResponseEntity.ok(evvExceptionService.getAuditLogs(id));
    }

    @PutMapping("/{id}/review")
    public EVVExceptionResponse reviewException(
            @PathVariable Long id,
            @RequestBody EVVExceptionReviewRequest request
    ) {
        return evvExceptionService.reviewException(id, request);
    }
    @GetMapping("/client/{clientId}")
    public List<EVVExceptionResponse> getExceptionsByClient(@PathVariable Long clientId) {
        return evvExceptionService.getExceptionsByClient(clientId);
    }
    @GetMapping("/compliance-dashboard")
    public ResponseEntity<EVVComplianceDashboardResponse> getComplianceDashboard() {
        return ResponseEntity.ok(evvExceptionService.getComplianceDashboard());
    }
}