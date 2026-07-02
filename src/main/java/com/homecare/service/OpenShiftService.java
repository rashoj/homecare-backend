package com.homecare.service;

import com.homecare.dto.AppointmentRequest;
import com.homecare.dto.AppointmentResponse;
import com.homecare.dto.OpenShiftRequest;
import com.homecare.dto.OpenShiftResponse;
import com.homecare.entity.Client;
import com.homecare.entity.OpenShift;
import com.homecare.entity.Organization;
import com.homecare.entity.User;
import com.homecare.repository.ClientCaregiverRepository;
import com.homecare.repository.ClientRepository;
import com.homecare.repository.OpenShiftRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OpenShiftService {

    private final OpenShiftRepository openShiftRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final AppointmentService appointmentService;
    private final ClientCaregiverRepository clientCaregiverRepository;

    public OpenShiftService(
            OpenShiftRepository openShiftRepository,
            ClientRepository clientRepository,
            UserRepository userRepository,
            AppointmentService appointmentService,
            ClientCaregiverRepository clientCaregiverRepository
    ) {
        this.openShiftRepository = openShiftRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.appointmentService = appointmentService;
        this.clientCaregiverRepository = clientCaregiverRepository;
    }

    public OpenShiftResponse createOpenShift(
            OpenShiftRequest request,
            String actorEmail
    ) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);

        Client client = clientRepository
                .findByIdAndOrganizationId(request.getClientId(), organization.getId())
                .orElseThrow(() -> new RuntimeException("Client not found for this organization."));

        validateShiftTimes(request);

        OpenShift openShift = OpenShift.builder()
                .organization(organization)
                .client(client)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .serviceType(defaultValue(request.getServiceType(), "PERSONAL_CARE"))
                .shiftType(defaultValue(request.getShiftType(), "OPEN_SHIFT"))
                .priority(defaultValue(request.getPriority(), "NORMAL"))
                .evvRequired(request.getEvvRequired() != null ? request.getEvvRequired() : true)
                .billable(request.getBillable() != null ? request.getBillable() : true)
                .requiredSkills(request.getRequiredSkills())
                .notes(request.getNotes())
                .createdByUserId(actor.getId())

                .status("OPEN")
                .assignedCaregiverOnly(
                        request.getAssignedCaregiverOnly() != null
                                ? request.getAssignedCaregiverOnly()
                                : false
                )
                .expiresAt(request.getExpiresAt())
                .build();

        return mapToResponse(openShiftRepository.save(openShift));
    }

    public OpenShiftResponse claimOpenShift(Long openShiftId, String actorEmail) {
        User caregiver = getActor(actorEmail);
        Organization organization = requireOrganization(caregiver);

        OpenShift openShift = openShiftRepository
                .findByIdAndOrganizationId(openShiftId, organization.getId())
                .orElseThrow(() -> new RuntimeException("Open shift not found for this organization."));

        if (!"OPEN".equalsIgnoreCase(openShift.getStatus())) {
            throw new RuntimeException("This open shift is no longer available.");
        }

        if (openShift.getExpiresAt() != null &&
                openShift.getExpiresAt().isBefore(LocalDateTime.now())) {
            openShift.setStatus("EXPIRED");
            openShiftRepository.save(openShift);
            throw new RuntimeException("This open shift has expired.");
        }

        if (Boolean.TRUE.equals(openShift.getAssignedCaregiverOnly())) {
            boolean assigned = clientCaregiverRepository
                    .existsByClientIdAndCaregiverIdAndActiveTrue(
                            openShift.getClient().getId(),
                            caregiver.getId()
                    );

            if (!assigned) {
                throw new RuntimeException(
                        "Only caregivers assigned to this client can claim this shift."
                );
            }
        }

        AppointmentRequest appointmentRequest = new AppointmentRequest();
        appointmentRequest.setClientId(openShift.getClient().getId());
        appointmentRequest.setCaregiverId(caregiver.getId());
        appointmentRequest.setStartTime(openShift.getStartTime());
        appointmentRequest.setEndTime(openShift.getEndTime());
        appointmentRequest.setServiceType(openShift.getServiceType());
        appointmentRequest.setShiftType("OPEN_SHIFT");
        appointmentRequest.setStatus("SCHEDULED");
        appointmentRequest.setEvvRequired(openShift.getEvvRequired());
        appointmentRequest.setBillable(openShift.getBillable());
        appointmentRequest.setRepeatType("NONE");
        appointmentRequest.setNotes(openShift.getNotes());

        AppointmentResponse appointment =
                appointmentService.createAppointmentFromOpenShift(
                        appointmentRequest,
                        caregiver,
                        openShift.getAssignedCaregiverOnly()
                );

        openShift.setClaimedByCaregiver(caregiver);
        openShift.setClaimedAt(LocalDateTime.now());
        openShift.setAssignedAt(LocalDateTime.now());
        openShift.setAppointmentId(appointment.getId());
        openShift.setStatus("ASSIGNED");

        return mapToResponse(openShiftRepository.save(openShift));
    }

    public List<OpenShiftResponse> getAllOpenShifts(String actorEmail) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);

        return openShiftRepository
                .findByOrganizationIdOrderByStartTimeAsc(organization.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<OpenShiftResponse> getOpenShifts(String actorEmail) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);

        return openShiftRepository
                .findByOrganizationIdAndStatusOrderByStartTimeAsc(
                        organization.getId(),
                        "OPEN"
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OpenShiftResponse cancelOpenShift(
            Long openShiftId,
            String actorEmail
    ) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);

        OpenShift openShift = openShiftRepository
                .findByIdAndOrganizationId(openShiftId, organization.getId())
                .orElseThrow(() -> new RuntimeException("Open shift not found for this organization."));

        if (!"OPEN".equalsIgnoreCase(openShift.getStatus())) {
            throw new RuntimeException("Only open shifts can be cancelled.");
        }

        openShift.setStatus("CANCELLED");

        return mapToResponse(openShiftRepository.save(openShift));
    }

    private void validateShiftTimes(OpenShiftRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new RuntimeException("Start time and end time are required.");
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new RuntimeException("End time must be after start time.");
        }
    }

    private User getActor(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found."));
    }

    private Organization requireOrganization(User user) {
        if (user.getOrganization() == null || user.getOrganization().getId() == null) {
            throw new RuntimeException("User is not assigned to an organization.");
        }

        return user.getOrganization();
    }

    private String defaultValue(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value.toUpperCase();
    }

    private OpenShiftResponse mapToResponse(OpenShift openShift) {
        return OpenShiftResponse.builder()
                .id(openShift.getId())
                .organizationId(openShift.getOrganization().getId())
                .clientId(openShift.getClient().getId())
                .clientName(openShift.getClient().getFullName())
                .claimedByCaregiverId(
                        openShift.getClaimedByCaregiver() != null
                                ? openShift.getClaimedByCaregiver().getId()
                                : null
                )
                .claimedByCaregiverName(
                        openShift.getClaimedByCaregiver() != null
                                ? openShift.getClaimedByCaregiver().getFullName()
                                : null
                )
                .startTime(openShift.getStartTime())
                .endTime(openShift.getEndTime())
                .serviceType(openShift.getServiceType())
                .shiftType(openShift.getShiftType())
                .status(openShift.getStatus())
                .priority(openShift.getPriority())
                .evvRequired(openShift.getEvvRequired())
                .billable(openShift.getBillable())
                .requiredSkills(openShift.getRequiredSkills())
                .notes(openShift.getNotes())
                .createdByUserId(openShift.getCreatedByUserId())
                .claimedAt(openShift.getClaimedAt())
                .assignedAt(openShift.getAssignedAt())
                .appointmentId(openShift.getAppointmentId())
                .createdAt(openShift.getCreatedAt())
                .updatedAt(openShift.getUpdatedAt())
                .assignedCaregiverOnly(openShift.getAssignedCaregiverOnly())
                .expiresAt(openShift.getExpiresAt())
                .build();
    }
}