package com.homecare.controller;

import com.homecare.dto.EVVComplianceDashboardResponse;
import com.homecare.dto.EVVExceptionResponse;
import com.homecare.dto.EVVExceptionReviewRequest;
import com.homecare.dto.EVVExceptionSummaryResponse;
import com.homecare.entity.EVVExceptionAuditLog;
import com.homecare.service.EVVExceptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public List<EVVExceptionResponse> getAllExceptions(Authentication authentication) {
        return evvExceptionService.getAllExceptions(authentication.getName());
    }

    @GetMapping("/open")
    public List<EVVExceptionResponse> getOpenExceptions(Authentication authentication) {
        return evvExceptionService.getOpenExceptions(authentication.getName());
    }

    @GetMapping("/summary")
    public ResponseEntity<EVVExceptionSummaryResponse> getSummary(Authentication authentication) {
        return ResponseEntity.ok(evvExceptionService.getSummary(authentication.getName()));
    }

    @GetMapping("/{id}/audit-logs")
    public ResponseEntity<List<EVVExceptionAuditLog>> getAuditLogs(@PathVariable Long id) {
        return ResponseEntity.ok(evvExceptionService.getAuditLogs(id));
    }

    @PutMapping("/{id}/review")
    public EVVExceptionResponse reviewException(
            @PathVariable Long id,
            @RequestBody EVVExceptionReviewRequest request,
            Authentication authentication
    ) {
        return evvExceptionService.reviewException(
                id,
                request,
                authentication.getName()
        );
    }

    @GetMapping("/client/{clientId}")
    public List<EVVExceptionResponse> getExceptionsByClient(
            @PathVariable Long clientId,
            Authentication authentication
    ) {
        return evvExceptionService.getExceptionsByClient(
                clientId,
                authentication.getName()
        );
    }

    @GetMapping("/compliance-dashboard")
    public ResponseEntity<EVVComplianceDashboardResponse> getComplianceDashboard(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                evvExceptionService.getComplianceDashboard(authentication.getName())
        );
    }
}