package com.homecare.service;

import com.homecare.dto.AppointmentRequest;
import com.homecare.dto.AppointmentResponse;
import com.homecare.dto.AppointmentStatusUpdateRequest;
import com.homecare.entity.Appointment;
import com.homecare.entity.Client;
import com.homecare.entity.Organization;
import com.homecare.entity.User;
import com.homecare.repository.AppointmentRepository;
import com.homecare.repository.ClientCaregiverRepository;
import com.homecare.repository.ClientRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ClientCaregiverRepository clientCaregiverRepository;
    private final AuditLogService auditLogService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            ClientRepository clientRepository,
            UserRepository userRepository,
            ClientCaregiverRepository clientCaregiverRepository,
            AuditLogService auditLogService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.clientCaregiverRepository = clientCaregiverRepository;
        this.auditLogService = auditLogService;
    }

    public AppointmentResponse createAppointment(
            AppointmentRequest request,
            String actorEmail
    ) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);

        Client client = clientRepository
                .findByIdAndOrganizationId(request.getClientId(), organization.getId())
                .orElseThrow(() -> new RuntimeException("Client not found for this organization"));

        User caregiver = userRepository.findById(request.getCaregiverId())
                .orElseThrow(() -> new RuntimeException("Caregiver not found"));

        if (caregiver.getOrganization() == null ||
                !caregiver.getOrganization().getId().equals(organization.getId())) {
            throw new RuntimeException("Caregiver does not belong to this organization");
        }

        boolean assigned = clientCaregiverRepository
                .existsByClientIdAndCaregiverIdAndActiveTrue(client.getId(), caregiver.getId());

        if (!assigned) {
            throw new RuntimeException("Caregiver must be assigned to the client before scheduling.");
        }

        validateAppointmentTimes(request);

        String repeatType = defaultValue(request.getRepeatType(), "NONE");

        if ("NONE".equals(repeatType)) {
            validateNoScheduleConflict(request);

            Appointment savedAppointment = appointmentRepository.save(
                    buildAppointment(request, client, caregiver, organization, repeatType)
            );

            auditLogService.logAction(
                    actor.getId(),
                    actor.getFullName(),
                    actor.getRole().name(),
                    client.getId(),
                    "CREATE_APPOINTMENT",
                    "APPOINTMENT",
                    savedAppointment.getId(),
                    "Appointment created."
            );

            return mapToResponse(savedAppointment);
        }

        List<Appointment> appointments = createRecurringAppointments(
                request,
                client,
                caregiver,
                organization,
                repeatType
        );

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                client.getId(),
                "CREATE_RECURRING_APPOINTMENTS",
                "APPOINTMENT",
                appointments.get(0).getId(),
                "Recurring appointments created. Count: " + appointments.size()
        );

        return mapToResponse(appointments.get(0));
    }

    public AppointmentResponse updateAppointmentStatus(
            Long appointmentId,
            AppointmentStatusUpdateRequest request,
            String actorEmail
    ) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);

        Appointment appointment = appointmentRepository
                .findByIdAndOrganizationId(appointmentId, organization.getId())
                .orElseThrow(() -> new RuntimeException("Appointment not found for this organization"));

        String previousStatus = appointment.getStatus();

        String status = request.getStatus() != null
                ? request.getStatus().toUpperCase()
                : appointment.getStatus();

        appointment.setStatus(status);
        appointment.setCompleted("COMPLETED".equals(status));

        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            appointment.setNotes(request.getNotes());
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);

        String action = "UPDATE_APPOINTMENT_STATUS";

        if ("COMPLETED".equals(status)) {
            action = "COMPLETE_APPOINTMENT";
        } else if ("CANCELLED".equals(status)) {
            action = "CANCEL_APPOINTMENT";
        }

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                savedAppointment.getClient().getId(),
                action,
                "APPOINTMENT",
                savedAppointment.getId(),
                "Appointment status changed from " + previousStatus + " to " + status + "."
        );

        return mapToResponse(savedAppointment);
    }

    public AppointmentResponse getAppointmentById(
            Long id,
            String actorEmail
    ) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);

        Appointment appointment = appointmentRepository
                .findByIdAndOrganizationId(id, organization.getId())
                .orElseThrow(() -> new RuntimeException("Appointment not found for this organization"));

        return mapToResponse(appointment);
    }

    public List<AppointmentResponse> getAllAppointments(String actorEmail) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);

        return appointmentRepository
                .findByOrganizationIdOrderByStartTimeDesc(organization.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AppointmentResponse> getAppointmentsByClient(
            Long clientId,
            String actorEmail
    ) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);

        return appointmentRepository
                .findByClientIdAndOrganizationId(clientId, organization.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AppointmentResponse> getAppointmentsByCaregiver(
            Long caregiverId,
            String actorEmail
    ) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);

        return appointmentRepository
                .findByCaregiverIdAndOrganizationId(caregiverId, organization.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void validateAppointmentTimes(AppointmentRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new RuntimeException("Start time and end time are required.");
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new RuntimeException("End time must be after start time.");
        }
    }

    private void validateNoScheduleConflict(AppointmentRequest request) {
        boolean caregiverConflict = appointmentRepository
                .findByCaregiverIdAndStartTimeLessThanAndEndTimeGreaterThan(
                        request.getCaregiverId(),
                        request.getEndTime(),
                        request.getStartTime()
                )
                .stream()
                .anyMatch(appointment -> !"CANCELLED".equalsIgnoreCase(appointment.getStatus()));

        if (caregiverConflict) {
            throw new RuntimeException("Caregiver already has an appointment during this time.");
        }

        boolean clientConflict = appointmentRepository
                .findByClientIdAndStartTimeLessThanAndEndTimeGreaterThan(
                        request.getClientId(),
                        request.getEndTime(),
                        request.getStartTime()
                )
                .stream()
                .anyMatch(appointment -> !"CANCELLED".equalsIgnoreCase(appointment.getStatus()));

        if (clientConflict) {
            throw new RuntimeException("Client already has an appointment during this time.");
        }
    }

    private Appointment buildAppointment(
            AppointmentRequest request,
            Client client,
            User caregiver,
            Organization organization,
            String repeatType
    ) {
        return Appointment.builder()
                .client(client)
                .caregiver(caregiver)
                .organization(organization)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .serviceType(defaultValue(request.getServiceType(), "PERSONAL_CARE"))
                .shiftType(defaultValue(request.getShiftType(), "REGULAR"))
                .status(defaultValue(request.getStatus(), "SCHEDULED"))
                .evvRequired(request.getEvvRequired() != null ? request.getEvvRequired() : true)
                .billable(request.getBillable() != null ? request.getBillable() : true)
                .repeatType(repeatType)
                .recurringGroupCreatedAt(LocalDateTime.now())
                .notes(request.getNotes())
                .completed(false)
                .build();
    }

    private List<Appointment> createRecurringAppointments(
            AppointmentRequest request,
            Client client,
            User caregiver,
            Organization organization,
            String repeatType
    ) {
        if (request.getRepeatUntil() == null) {
            throw new RuntimeException("Repeat until date is required for recurring appointments.");
        }

        if (!"DAILY".equals(repeatType) && !"WEEKLY".equals(repeatType)) {
            throw new RuntimeException("Repeat type must be NONE, DAILY, or WEEKLY.");
        }

        List<Appointment> appointments = new java.util.ArrayList<>();

        LocalDateTime currentStart = request.getStartTime();
        LocalDateTime currentEnd = request.getEndTime();
        LocalDateTime recurringGroupCreatedAt = LocalDateTime.now();

        while (!currentStart.toLocalDate().isAfter(request.getRepeatUntil())) {
            AppointmentRequest occurrenceRequest = copyRequestWithTimes(
                    request,
                    currentStart,
                    currentEnd
            );

            validateNoScheduleConflict(occurrenceRequest);

            Appointment appointment = buildAppointment(
                    occurrenceRequest,
                    client,
                    caregiver,
                    organization,
                    repeatType
            );

            appointment.setRecurringGroupCreatedAt(recurringGroupCreatedAt);
            appointments.add(appointment);

            if ("DAILY".equals(repeatType)) {
                currentStart = currentStart.plusDays(1);
                currentEnd = currentEnd.plusDays(1);
            } else {
                currentStart = currentStart.plusWeeks(1);
                currentEnd = currentEnd.plusWeeks(1);
            }
        }

        return appointmentRepository.saveAll(appointments);
    }

    private AppointmentRequest copyRequestWithTimes(
            AppointmentRequest original,
            LocalDateTime start,
            LocalDateTime end
    ) {
        AppointmentRequest copy = new AppointmentRequest();

        copy.setClientId(original.getClientId());
        copy.setCaregiverId(original.getCaregiverId());
        copy.setCreatedByUserId(original.getCreatedByUserId());
        copy.setStartTime(start);
        copy.setEndTime(end);
        copy.setServiceType(original.getServiceType());
        copy.setShiftType(original.getShiftType());
        copy.setStatus(original.getStatus());
        copy.setEvvRequired(original.getEvvRequired());
        copy.setBillable(original.getBillable());
        copy.setRepeatType(original.getRepeatType());
        copy.setRepeatUntil(original.getRepeatUntil());
        copy.setNotes(original.getNotes());

        return copy;
    }

    private User getActor(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Organization requireOrganization(User user) {
        if (user.getOrganization() == null || user.getOrganization().getId() == null) {
            throw new RuntimeException(
                    "User is not assigned to an organization. userId="
                            + user.getId()
                            + ", email="
                            + user.getEmail()
                            + ", role="
                            + user.getRole()
            );
        }

        return user.getOrganization();
    }

    private String defaultValue(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value.toUpperCase();
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .clientId(appointment.getClient().getId())
                .clientName(appointment.getClient().getFullName())
                .caregiverId(appointment.getCaregiver().getId())
                .caregiverName(appointment.getCaregiver().getFullName())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .serviceType(appointment.getServiceType())
                .shiftType(appointment.getShiftType())
                .status(appointment.getStatus())
                .evvRequired(appointment.getEvvRequired())
                .billable(appointment.getBillable())
                .notes(appointment.getNotes())
                .completed(appointment.getCompleted())
                .repeatType(appointment.getRepeatType())
                .recurringGroupCreatedAt(appointment.getRecurringGroupCreatedAt())
                .build();
    }
}