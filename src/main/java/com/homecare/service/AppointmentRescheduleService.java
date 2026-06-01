package com.homecare.service;

import com.homecare.dto.AppointmentRescheduleRequestDto;
import com.homecare.dto.AppointmentRescheduleResponse;
import com.homecare.dto.AppointmentRescheduleReviewRequest;
import com.homecare.entity.Appointment;
import com.homecare.entity.AppointmentRescheduleRequest;
import com.homecare.entity.User;
import com.homecare.repository.AppointmentRepository;
import com.homecare.repository.AppointmentRescheduleRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentRescheduleService {

    private final AppointmentRescheduleRepository rescheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public AppointmentRescheduleService(
            AppointmentRescheduleRepository rescheduleRepository,
            AppointmentRepository appointmentRepository,
            UserRepository userRepository,
            AuditLogService auditLogService
    ) {
        this.rescheduleRepository = rescheduleRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public AppointmentRescheduleResponse createRequest(
            AppointmentRescheduleRequestDto request
    ) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found."));

        User requestedBy = userRepository.findById(request.getRequestedByUserId())
                .orElseThrow(() -> new RuntimeException("Requesting user not found."));

        validateRequestedTimes(request.getRequestedStartTime(), request.getRequestedEndTime());

        AppointmentRescheduleRequest rescheduleRequest =
                AppointmentRescheduleRequest.builder()
                        .appointment(appointment)
                        .client(appointment.getClient())
                        .caregiver(appointment.getCaregiver())
                        .requestedBy(requestedBy)
                        .originalStartTime(appointment.getStartTime())
                        .originalEndTime(appointment.getEndTime())
                        .requestedStartTime(request.getRequestedStartTime())
                        .requestedEndTime(request.getRequestedEndTime())
                        .reason(request.getReason())
                        .status("SUBMITTED")
                        .build();

        AppointmentRescheduleRequest savedRequest =
                rescheduleRepository.save(rescheduleRequest);

        auditLogService.logAction(
                requestedBy.getId(),
                requestedBy.getFullName(),
                requestedBy.getRole().name(),
                appointment.getClient().getId(),
                "CREATE_RESCHEDULE_REQUEST",
                "APPOINTMENT_RESCHEDULE_REQUEST",
                savedRequest.getId(),
                "User submitted appointment reschedule request."
        );

        return mapToResponse(savedRequest);
    }

    public List<AppointmentRescheduleResponse> getAllRequests() {
        return rescheduleRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AppointmentRescheduleResponse> getRequestsByClient(Long clientId) {
        return rescheduleRepository.findByClientIdOrderByCreatedAtDesc(clientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AppointmentRescheduleResponse> getRequestsByCaregiver(Long caregiverId) {
        return rescheduleRepository.findByCaregiverIdOrderByCreatedAtDesc(caregiverId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AppointmentRescheduleResponse> getRequestsByAppointment(Long appointmentId) {
        return rescheduleRepository.findByAppointmentIdOrderByCreatedAtDesc(appointmentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AppointmentRescheduleResponse reviewRequest(
            Long requestId,
            AppointmentRescheduleReviewRequest request
    ) {
        AppointmentRescheduleRequest rescheduleRequest =
                rescheduleRepository.findById(requestId)
                        .orElseThrow(() -> new RuntimeException("Reschedule request not found."));

        User reviewer = userRepository.findById(request.getReviewedByUserId())
                .orElseThrow(() -> new RuntimeException("Reviewer not found."));

        String status = defaultValue(request.getStatus(), "UNDER_REVIEW");

        if (!List.of("UNDER_REVIEW", "APPROVED", "REJECTED").contains(status)) {
            throw new RuntimeException("Invalid reschedule request status.");
        }

        rescheduleRequest.setStatus(status);
        rescheduleRequest.setAdminNotes(request.getAdminNotes());
        rescheduleRequest.setReviewedBy(reviewer);
        rescheduleRequest.setReviewedAt(LocalDateTime.now());

        if ("APPROVED".equals(status)) {
            applyApprovedReschedule(rescheduleRequest);

            auditLogService.logAction(
                    reviewer.getId(),
                    reviewer.getFullName(),
                    reviewer.getRole().name(),
                    rescheduleRequest.getClient().getId(),
                    "APPROVE_RESCHEDULE_REQUEST",
                    "APPOINTMENT_RESCHEDULE_REQUEST",
                    rescheduleRequest.getId(),
                    "Reschedule request approved."
            );
        }

        if ("REJECTED".equals(status)) {
            auditLogService.logAction(
                    reviewer.getId(),
                    reviewer.getFullName(),
                    reviewer.getRole().name(),
                    rescheduleRequest.getClient().getId(),
                    "REJECT_RESCHEDULE_REQUEST",
                    "APPOINTMENT_RESCHEDULE_REQUEST",
                    rescheduleRequest.getId(),
                    "Reschedule request rejected."
            );
        }

        if ("UNDER_REVIEW".equals(status)) {
            auditLogService.logAction(
                    reviewer.getId(),
                    reviewer.getFullName(),
                    reviewer.getRole().name(),
                    rescheduleRequest.getClient().getId(),
                    "REVIEW_RESCHEDULE_REQUEST",
                    "APPOINTMENT_RESCHEDULE_REQUEST",
                    rescheduleRequest.getId(),
                    "Reschedule request marked under review."
            );
        }

        AppointmentRescheduleRequest savedRequest =
                rescheduleRepository.save(rescheduleRequest);

        return mapToResponse(savedRequest);
    }

    private void applyApprovedReschedule(
            AppointmentRescheduleRequest rescheduleRequest
    ) {
        Appointment appointment = rescheduleRequest.getAppointment();

        validateRequestedTimes(
                rescheduleRequest.getRequestedStartTime(),
                rescheduleRequest.getRequestedEndTime()
        );

        validateNoScheduleConflict(
                appointment.getId(),
                appointment.getClient().getId(),
                appointment.getCaregiver().getId(),
                rescheduleRequest.getRequestedStartTime(),
                rescheduleRequest.getRequestedEndTime()
        );

        appointment.setStartTime(rescheduleRequest.getRequestedStartTime());
        appointment.setEndTime(rescheduleRequest.getRequestedEndTime());
        appointment.setNotes(
                appendNote(
                        appointment.getNotes(),
                        "Appointment rescheduled through approved request #" +
                                rescheduleRequest.getId()
                )
        );

        Appointment savedAppointment = appointmentRepository.save(appointment);

        auditLogService.logAction(
                rescheduleRequest.getReviewedBy().getId(),
                rescheduleRequest.getReviewedBy().getFullName(),
                rescheduleRequest.getReviewedBy().getRole().name(),
                savedAppointment.getClient().getId(),
                "UPDATE_APPOINTMENT_FROM_RESCHEDULE",
                "APPOINTMENT",
                savedAppointment.getId(),
                "Appointment updated from approved reschedule request #" +
                        rescheduleRequest.getId()
        );
    }

    private void validateRequestedTimes(
            LocalDateTime requestedStartTime,
            LocalDateTime requestedEndTime
    ) {
        if (requestedStartTime == null || requestedEndTime == null) {
            throw new RuntimeException("Requested start and end time are required.");
        }

        if (!requestedEndTime.isAfter(requestedStartTime)) {
            throw new RuntimeException("Requested end time must be after start time.");
        }
    }

    private void validateNoScheduleConflict(
            Long currentAppointmentId,
            Long clientId,
            Long caregiverId,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        boolean caregiverConflict = appointmentRepository
                .findByCaregiverIdAndStartTimeLessThanAndEndTimeGreaterThan(
                        caregiverId,
                        endTime,
                        startTime
                )
                .stream()
                .anyMatch(appointment ->
                        !appointment.getId().equals(currentAppointmentId) &&
                                !"CANCELLED".equalsIgnoreCase(appointment.getStatus())
                );

        if (caregiverConflict) {
            throw new RuntimeException("Caregiver already has another appointment during this time.");
        }

        boolean clientConflict = appointmentRepository
                .findByClientIdAndStartTimeLessThanAndEndTimeGreaterThan(
                        clientId,
                        endTime,
                        startTime
                )
                .stream()
                .anyMatch(appointment ->
                        !appointment.getId().equals(currentAppointmentId) &&
                                !"CANCELLED".equalsIgnoreCase(appointment.getStatus())
                );

        if (clientConflict) {
            throw new RuntimeException("Client already has another appointment during this time.");
        }
    }

    private String appendNote(String existingNotes, String newNote) {
        if (existingNotes == null || existingNotes.isBlank()) {
            return newNote;
        }

        return existingNotes + "\n" + newNote;
    }

    private String defaultValue(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value.toUpperCase();
    }

    private AppointmentRescheduleResponse mapToResponse(
            AppointmentRescheduleRequest request
    ) {
        return AppointmentRescheduleResponse.builder()
                .id(request.getId())
                .appointmentId(request.getAppointment().getId())
                .clientId(request.getClient().getId())
                .clientName(request.getClient().getFullName())
                .caregiverId(request.getCaregiver().getId())
                .caregiverName(request.getCaregiver().getFullName())
                .requestedByUserId(request.getRequestedBy().getId())
                .requestedByName(request.getRequestedBy().getFullName())
                .originalStartTime(request.getOriginalStartTime())
                .originalEndTime(request.getOriginalEndTime())
                .requestedStartTime(request.getRequestedStartTime())
                .requestedEndTime(request.getRequestedEndTime())
                .reason(request.getReason())
                .status(request.getStatus())
                .adminNotes(request.getAdminNotes())
                .reviewedByUserId(
                        request.getReviewedBy() != null
                                ? request.getReviewedBy().getId()
                                : null
                )
                .reviewedByName(
                        request.getReviewedBy() != null
                                ? request.getReviewedBy().getFullName()
                                : null
                )
                .reviewedAt(request.getReviewedAt())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}