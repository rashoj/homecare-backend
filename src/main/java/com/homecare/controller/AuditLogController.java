package com.homecare.controller;

import com.homecare.dto.AuditLogRequest;
import com.homecare.dto.AuditLogResponse;
import com.homecare.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin("*")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @PostMapping
    public AuditLogResponse createLog(
            @RequestBody AuditLogRequest request,
            HttpServletRequest servletRequest
    ) {
        return auditLogService.createLog(
                request,
                getClientIp(servletRequest),
                servletRequest.getHeader("User-Agent")
        );
    }

    @GetMapping
    public List<AuditLogResponse> getAllLogs() {
        return auditLogService.getAllLogs();
    }

    @GetMapping("/client/{clientId}")
    public List<AuditLogResponse> getLogsByClient(@PathVariable Long clientId) {
        return auditLogService.getLogsByClient(clientId);
    }

    @GetMapping("/actor/{actorUserId}")
    public List<AuditLogResponse> getLogsByActor(@PathVariable Long actorUserId) {
        return auditLogService.getLogsByActor(actorUserId);
    }

    @GetMapping("/resource/{resourceType}/{resourceId}")
    public List<AuditLogResponse> getLogsByResource(
            @PathVariable String resourceType,
            @PathVariable Long resourceId
    ) {
        return auditLogService.getLogsByResource(resourceType, resourceId);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}