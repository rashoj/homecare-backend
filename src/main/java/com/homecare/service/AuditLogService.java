package com.homecare.service;

import com.homecare.dto.AuditLogRequest;
import com.homecare.dto.AuditLogResponse;
import com.homecare.entity.AuditLog;
import com.homecare.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public AuditLogResponse createLog(
            AuditLogRequest request,
            String ipAddress,
            String userAgent
    ) {
        AuditLog log = AuditLog.builder()
                .actorUserId(request.getActorUserId())
                .actorName(request.getActorName())
                .actorRole(request.getActorRole())
                .clientId(request.getClientId())
                .action(request.getAction())
                .resourceType(request.getResourceType())
                .resourceId(request.getResourceId())
                .description(request.getDescription())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        return mapToResponse(auditLogRepository.save(log));
    }

    public List<AuditLogResponse> getAllLogs() {
        return auditLogRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AuditLogResponse> getLogsByClient(Long clientId) {
        return auditLogRepository.findByClientIdOrderByCreatedAtDesc(clientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AuditLogResponse> getLogsByActor(Long actorUserId) {
        return auditLogRepository.findByActorUserIdOrderByCreatedAtDesc(actorUserId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AuditLogResponse> getLogsByResource(String resourceType, Long resourceId) {
        return auditLogRepository
                .findByResourceTypeAndResourceIdOrderByCreatedAtDesc(
                        resourceType,
                        resourceId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .actorUserId(log.getActorUserId())
                .actorName(log.getActorName())
                .actorRole(log.getActorRole())
                .clientId(log.getClientId())
                .action(log.getAction())
                .resourceType(log.getResourceType())
                .resourceId(log.getResourceId())
                .description(log.getDescription())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .createdAt(log.getCreatedAt())
                .build();
    }public void logAction(
            Long actorUserId,
            String actorName,
            String actorRole,
            Long clientId,
            String action,
            String resourceType,
            Long resourceId,
            String description
    ) {
        AuditLog log = AuditLog.builder()
                .actorUserId(actorUserId)
                .actorName(actorName)
                .actorRole(actorRole)
                .clientId(clientId)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .description(description)
                .ipAddress("SYSTEM")
                .userAgent("BACKEND_SERVICE")
                .build();

        auditLogRepository.save(log);
    }
}