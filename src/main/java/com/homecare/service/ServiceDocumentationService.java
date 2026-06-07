package com.homecare.service;

import com.homecare.dto.*;
import com.homecare.entity.*;
import com.homecare.repository.*;
import org.springframework.stereotype.Service;
import com.homecare.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServiceDocumentationService {

    private final ServiceDocumentationRepository serviceDocumentationRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClockRecordRepository clockRecordRepository;
    private final TimesheetService timesheetService;
    private final ServiceDocumentationAuditLogRepository auditLogRepository;
    private final ISPGoalRepository ispGoalRepository;
    private final ISPGoalProgressLogRepository ispGoalProgressLogRepository;
    private final BehaviorEventRepository behaviorEventRepository;
    private final UserRepository userRepository;

    public ServiceDocumentationService(
            ServiceDocumentationRepository serviceDocumentationRepository,
            AppointmentRepository appointmentRepository,
            ClockRecordRepository clockRecordRepository,
            TimesheetService timesheetService,
            ServiceDocumentationAuditLogRepository auditLogRepository,
            ISPGoalRepository ispGoalRepository,
            ISPGoalProgressLogRepository ispGoalProgressLogRepository,
            BehaviorEventRepository behaviorEventRepository,UserRepository userRepository
    ) {
        this.serviceDocumentationRepository = serviceDocumentationRepository;
        this.appointmentRepository = appointmentRepository;
        this.clockRecordRepository = clockRecordRepository;
        this.timesheetService = timesheetService;
        this.auditLogRepository = auditLogRepository;
        this.ispGoalRepository = ispGoalRepository;
        this.ispGoalProgressLogRepository = ispGoalProgressLogRepository;
        this.behaviorEventRepository = behaviorEventRepository;
        this.userRepository = userRepository;
    }

    public ServiceDocumentationResponse submitDocumentation(ServiceDocumentationRequest request, String actorEmail) {
        if (serviceDocumentationRepository.existsByAppointmentId(request.getAppointmentId())) {
            throw new RuntimeException("Service documentation already exists for this appointment.");
        }

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found."));

        if (!Boolean.TRUE.equals(request.getShiftCompleted())) {
            throw new RuntimeException("Shift completion verification is required.");
        }

        if (request.getCaregiverSignature() == null || request.getCaregiverSignature().isBlank()) {
            throw new RuntimeException("Caregiver signature is required.");
        }

        ServiceDocumentation documentation = ServiceDocumentation.builder()
                .appointment(appointment)
                .client(appointment.getClient())
                .caregiver(appointment.getCaregiver())
                .organization(appointment.getOrganization())
                .shiftTasksCompleted(request.getShiftTasksCompleted())
                .adlsCompleted(request.getAdlsCompleted())
                .goalProgressNotes(request.getGoalProgressNotes())
                .dailyServiceNotes(request.getDailyServiceNotes())
                .shiftCompleted(request.getShiftCompleted())
                .caregiverSignature(request.getCaregiverSignature())
                .status("SUBMITTED")
                .locked(false)
                .submittedAt(LocalDateTime.now())
                .build();

        ServiceDocumentation savedDocumentation =
                serviceDocumentationRepository.save(documentation);

        if (request.getIspGoalProgress() != null) {
            for (ISPGoalProgressSubmissionRequest progressRequest
                    : request.getIspGoalProgress()) {

                ISPGoal goal = ispGoalRepository.findById(progressRequest.getGoalId())
                        .orElseThrow(() -> new RuntimeException("ISP goal not found."));

                ISPGoalProgressLog progressLog = ISPGoalProgressLog.builder()
                        .goal(goal)
                        .client(appointment.getClient())
                        .caregiver(appointment.getCaregiver())
                        .appointment(appointment)
                        .serviceDocumentation(savedDocumentation)
                        .progressStatus(progressRequest.getProgressStatus())
                        .promptLevel(progressRequest.getPromptLevel())
                        .progressNote(progressRequest.getProgressNote())
                        .createdAt(LocalDateTime.now())
                        .build();

                ispGoalProgressLogRepository.save(progressLog);
            }
            if (request.getBehaviorEvents() != null) {
                for (BehaviorEventSubmissionRequest behaviorRequest
                        : request.getBehaviorEvents()) {

                    BehaviorEvent behaviorEvent = BehaviorEvent.builder()
                            .client(appointment.getClient())
                            .caregiver(appointment.getCaregiver())
                            .appointment(appointment)
                            .serviceDocumentation(savedDocumentation)
                            .behaviorType(behaviorRequest.getBehaviorType())
                            .trigger(behaviorRequest.getTrigger())
                            .severity(behaviorRequest.getSeverity())
                            .durationMinutes(behaviorRequest.getDurationMinutes())
                            .interventionUsed(behaviorRequest.getInterventionUsed())
                            .outcome(behaviorRequest.getOutcome())
                            .notes(behaviorRequest.getNotes())
                            .createdAt(LocalDateTime.now())
                            .build();

                    behaviorEventRepository.save(behaviorEvent);
                }
            }
        }

        return mapToResponse(savedDocumentation);
    }

    public ServiceDocumentationResponse reviewDocumentation(
            Long id,
            ServiceDocumentationReviewRequest request,
            String actorEmail
    ) {
        ServiceDocumentation documentation = serviceDocumentationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service documentation not found."));

        if (Boolean.TRUE.equals(documentation.getLocked())) {
            throw new RuntimeException("Finalized documentation is locked and cannot be changed.");
        }

        String oldStatus = documentation.getStatus();
        LocalDateTime oldClockInTime = documentation.getCorrectedClockInTime();
        LocalDateTime oldClockOutTime = documentation.getCorrectedClockOutTime();

        String status = request.getStatus() != null
                ? request.getStatus().toUpperCase()
                : "REVIEWED";

        documentation.setStatus(status);
        documentation.setSupervisorComments(request.getSupervisorComments());

        if ("APPROVED".equals(status)) {
            ClockRecord clockRecord = clockRecordRepository
                    .findByAppointmentId(documentation.getAppointment().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Cannot generate billing record. Clock record not found for this appointment."
                    ));

            LocalDateTime effectiveClockIn =
                    request.getCorrectedClockInTime() != null
                            ? request.getCorrectedClockInTime()
                            : clockRecord.getClockInTime();

            LocalDateTime effectiveClockOut =
                    request.getCorrectedClockOutTime() != null
                            ? request.getCorrectedClockOutTime()
                            : clockRecord.getClockOutTime();

            if (effectiveClockIn == null || effectiveClockOut == null) {
                throw new RuntimeException("Clock-in and clock-out times are required before approval.");
            }

            if (effectiveClockOut.isBefore(effectiveClockIn)) {
                throw new RuntimeException("Clock-out time cannot be before clock-in time.");
            }

            if (request.getCorrectedClockInTime() != null) {
                clockRecord.setClockInTime(request.getCorrectedClockInTime());
            }

            if (request.getCorrectedClockOutTime() != null) {
                clockRecord.setClockOutTime(request.getCorrectedClockOutTime());
            }

            long minutes = java.time.Duration.between(
                    clockRecord.getClockInTime(),
                    clockRecord.getClockOutTime()
            ).toMinutes();

            clockRecord.setTotalHours(minutes / 60.0);
            clockRecordRepository.save(clockRecord);

            documentation.setCorrectedClockInTime(request.getCorrectedClockInTime());
            documentation.setCorrectedClockOutTime(request.getCorrectedClockOutTime());
            documentation.setCorrectionReason(request.getCorrectionReason());
            documentation.setTimeCorrectionApproved(
                    request.getTimeCorrectionApproved() != null
                            ? request.getTimeCorrectionApproved()
                            : true
            );

            documentation.setApprovedAt(LocalDateTime.now());
            documentation.setLocked(true);

            timesheetService.generateFromClockRecord(clockRecord.getId());
        }

        ServiceDocumentation savedDocumentation =
                serviceDocumentationRepository.save(documentation);

        ServiceDocumentationAuditLog auditLog =
                ServiceDocumentationAuditLog.builder()
                        .documentationId(savedDocumentation.getId())
                        .oldStatus(oldStatus)
                        .newStatus(savedDocumentation.getStatus())
                        .oldClockInTime(oldClockInTime)
                        .oldClockOutTime(oldClockOutTime)
                        .correctedClockInTime(savedDocumentation.getCorrectedClockInTime())
                        .correctedClockOutTime(savedDocumentation.getCorrectedClockOutTime())
                        .correctionReason(savedDocumentation.getCorrectionReason())
                        .supervisorComments(savedDocumentation.getSupervisorComments())
                        .timeCorrectionApproved(savedDocumentation.getTimeCorrectionApproved())
                        .reviewedAt(LocalDateTime.now())
                        .createdAt(LocalDateTime.now())
                        .build();

        auditLogRepository.save(auditLog);

        return mapToResponse(savedDocumentation);
    }

    public List<ServiceDocumentationResponse> getDocumentationByClient(
            Long clientId,
            String actorEmail
    ){
        Organization organization = getOrganization(actorEmail);

        return serviceDocumentationRepository
                .findByOrganizationIdAndClientIdOrderBySubmittedAtDesc(
                        organization.getId(),
                        clientId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ServiceDocumentationResponse> getPendingDocumentation(String actorEmail) {
        Organization organization = getOrganization(actorEmail);

        return serviceDocumentationRepository
                .findByOrganizationIdAndStatusOrderBySubmittedAtDesc(
                        organization.getId(),
                        "SUBMITTED"
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ServiceDocumentationResponse getDocumentationByAppointment(Long appointmentId, String actorEmail) {
        Organization organization = getOrganization(actorEmail);

        ServiceDocumentation documentation =
                serviceDocumentationRepository
                        .findByAppointmentIdAndOrganizationId(
                                appointmentId,
                                organization.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException("Service documentation not found.")
                        );

        return mapToResponse(documentation);
    }

    public List<ServiceDocumentationAuditLog> getAuditLogs(Long documentationId) {
        return auditLogRepository.findByDocumentationIdOrderByCreatedAtDesc(documentationId);
    }

    private ServiceDocumentationResponse mapToResponse(ServiceDocumentation documentation) {
        return ServiceDocumentationResponse.builder()
                .id(documentation.getId())
                .appointmentId(documentation.getAppointment().getId())
                .clientId(documentation.getClient().getId())
                .clientName(documentation.getClient().getFullName())
                .caregiverId(documentation.getCaregiver().getId())
                .caregiverName(documentation.getCaregiver().getFullName())
                .shiftTasksCompleted(documentation.getShiftTasksCompleted())
                .adlsCompleted(documentation.getAdlsCompleted())
                .goalProgressNotes(documentation.getGoalProgressNotes())
                .dailyServiceNotes(documentation.getDailyServiceNotes())
                .shiftCompleted(documentation.getShiftCompleted())
                .caregiverSignature(documentation.getCaregiverSignature())
                .status(documentation.getStatus())
                .locked(documentation.getLocked())
                .supervisorComments(documentation.getSupervisorComments())
                .submittedAt(documentation.getSubmittedAt())
                .approvedAt(documentation.getApprovedAt())
                .correctedClockInTime(documentation.getCorrectedClockInTime())
                .correctedClockOutTime(documentation.getCorrectedClockOutTime())
                .correctionReason(documentation.getCorrectionReason())
                .timeCorrectionApproved(documentation.getTimeCorrectionApproved())
                .build();
    }
    private Organization getOrganization(String actorEmail) {

        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (actor.getOrganization() == null) {
            throw new RuntimeException(
                    "User is not assigned to an organization"
            );
        }

        return actor.getOrganization();
    }
}