package com.homecare.service;

import com.homecare.dto.EVVComplianceDashboardResponse;
import com.homecare.dto.EVVExceptionResponse;
import com.homecare.dto.EVVExceptionReviewRequest;
import com.homecare.dto.EVVExceptionSummaryResponse;
import com.homecare.entity.EVVException;
import com.homecare.entity.EVVExceptionAuditLog;
import com.homecare.entity.Organization;
import com.homecare.entity.User;
import com.homecare.repository.EVVExceptionAuditLogRepository;
import com.homecare.repository.EVVExceptionRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class EVVExceptionService {

    private final EVVExceptionRepository evvExceptionRepository;
    private final EVVExceptionAuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public EVVExceptionService(
            EVVExceptionRepository evvExceptionRepository,
            EVVExceptionAuditLogRepository auditLogRepository,
            UserRepository userRepository
    ) {
        this.evvExceptionRepository = evvExceptionRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    public List<EVVExceptionResponse> getAllExceptions(String actorEmail) {
        Organization organization = getOrganization(actorEmail);

        return evvExceptionRepository
                .findByOrganizationIdOrderByCreatedAtDesc(organization.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<EVVExceptionResponse> getOpenExceptions(String actorEmail) {
        Organization organization = getOrganization(actorEmail);

        return evvExceptionRepository
                .findByOrganizationIdAndStatusOrderByCreatedAtDesc(
                        organization.getId(),
                        "OPEN"
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public EVVExceptionResponse reviewException(
            Long id,
            EVVExceptionReviewRequest request,
            String actorEmail
    ) {
        Organization organization = getOrganization(actorEmail);

        EVVException exception = evvExceptionRepository
                .findByIdAndOrganizationId(id, organization.getId())
                .orElseThrow(() -> new RuntimeException("EVV exception not found for this organization."));

        String oldStatus = exception.getStatus();

        String status = request.getStatus() != null
                ? request.getStatus().toUpperCase()
                : "REVIEWED";

        if (!"REVIEWED".equals(status) && !"RESOLVED".equals(status)) {
            throw new RuntimeException("Status must be REVIEWED or RESOLVED.");
        }

        LocalDateTime now = LocalDateTime.now();

        exception.setStatus(status);
        exception.setSupervisorNotes(request.getSupervisorNotes());
        exception.setAdminResolutionReason(request.getAdminResolutionReason());
        exception.setCorrectedClockOutTime(request.getCorrectedClockOutTime());
        exception.setAdminApproved(request.getAdminApproved());
        exception.setReviewedAt(now);

        EVVException savedException = evvExceptionRepository.save(exception);

        EVVExceptionAuditLog auditLog = EVVExceptionAuditLog.builder()
                .exceptionId(savedException.getId())
                .oldStatus(oldStatus)
                .newStatus(status)
                .supervisorNotes(request.getSupervisorNotes())
                .reviewedBy(null)
                .reviewedAt(now)
                .createdAt(now)
                .build();

        auditLogRepository.save(auditLog);

        return mapToResponse(savedException);
    }

    public List<EVVExceptionAuditLog> getAuditLogs(Long exceptionId) {
        return auditLogRepository.findByExceptionIdOrderByCreatedAtDesc(exceptionId);
    }

    public EVVExceptionSummaryResponse getSummary(String actorEmail) {
        Organization organization = getOrganization(actorEmail);

        List<EVVException> exceptions =
                evvExceptionRepository.findByOrganizationIdOrderByCreatedAtDesc(
                        organization.getId()
                );

        long total = exceptions.size();

        long needsReview = exceptions.stream()
                .filter(e -> "OPEN".equals(e.getStatus()))
                .count();

        long reviewed = exceptions.stream()
                .filter(e -> "REVIEWED".equals(e.getStatus()))
                .count();

        long resolved = exceptions.stream()
                .filter(e -> "RESOLVED".equals(e.getStatus()))
                .count();

        long highSeverity = exceptions.stream()
                .filter(e -> "HIGH".equals(e.getSeverity()))
                .count();

        return EVVExceptionSummaryResponse.builder()
                .total(total)
                .needsReview(needsReview)
                .reviewed(reviewed)
                .resolved(resolved)
                .highSeverity(highSeverity)
                .build();
    }

    public List<EVVExceptionResponse> getExceptionsByClient(
            Long clientId,
            String actorEmail
    ) {
        Organization organization = getOrganization(actorEmail);

        return evvExceptionRepository
                .findByOrganizationIdAndClientIdOrderByCreatedAtDesc(
                        organization.getId(),
                        clientId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public EVVComplianceDashboardResponse getComplianceDashboard(String actorEmail) {
        Organization organization = getOrganization(actorEmail);

        List<EVVException> exceptions =
                evvExceptionRepository.findByOrganizationIdOrderByCreatedAtDesc(
                        organization.getId()
                );

        long total = exceptions.size();

        long open = exceptions.stream()
                .filter(e -> "OPEN".equals(e.getStatus()))
                .count();

        long reviewed = exceptions.stream()
                .filter(e -> "REVIEWED".equals(e.getStatus()))
                .count();

        long resolved = exceptions.stream()
                .filter(e -> "RESOLVED".equals(e.getStatus()))
                .count();

        long highSeverity = exceptions.stream()
                .filter(e -> "HIGH".equals(e.getSeverity()))
                .count();

        double complianceRate = total == 0
                ? 100.0
                : ((double) resolved / total) * 100;

        Map<String, Long> byType = exceptions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        EVVException::getExceptionType,
                        java.util.stream.Collectors.counting()
                ));

        Map<String, Long> byClient = exceptions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        e -> e.getClient().getFullName(),
                        java.util.stream.Collectors.counting()
                ));

        Map<String, Long> byCaregiver = exceptions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        e -> e.getCaregiver().getFullName(),
                        java.util.stream.Collectors.counting()
                ));

        return EVVComplianceDashboardResponse.builder()
                .totalExceptions(total)
                .openExceptions(open)
                .reviewedExceptions(reviewed)
                .resolvedExceptions(resolved)
                .highSeverityExceptions(highSeverity)
                .complianceRate(complianceRate)
                .exceptionsByType(byType)
                .exceptionsByClient(byClient)
                .exceptionsByCaregiver(byCaregiver)
                .build();
    }

    private Organization getOrganization(String actorEmail) {
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (actor.getOrganization() == null || actor.getOrganization().getId() == null) {
            throw new RuntimeException(
                    "User is not assigned to an organization. userId="
                            + actor.getId()
                            + ", email="
                            + actor.getEmail()
                            + ", role="
                            + actor.getRole()
            );
        }

        return actor.getOrganization();
    }

    private EVVExceptionResponse mapToResponse(EVVException exception) {
        return EVVExceptionResponse.builder()
                .id(exception.getId())
                .appointmentId(exception.getAppointment().getId())
                .clockRecordId(
                        exception.getClockRecord() != null
                                ? exception.getClockRecord().getId()
                                : null
                )
                .clientId(exception.getClient().getId())
                .clientName(exception.getClient().getFullName())
                .caregiverId(exception.getCaregiver().getId())
                .caregiverName(exception.getCaregiver().getFullName())
                .exceptionType(exception.getExceptionType())
                .severity(exception.getSeverity())
                .status(exception.getStatus())
                .description(exception.getDescription())
                .supervisorNotes(exception.getSupervisorNotes())
                .reviewedAt(exception.getReviewedAt())
                .createdAt(exception.getCreatedAt())
                .adminResolutionReason(exception.getAdminResolutionReason())
                .correctedClockOutTime(exception.getCorrectedClockOutTime())
                .adminApproved(exception.getAdminApproved())
                .build();
    }
}