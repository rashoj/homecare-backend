package com.homecare.service;

import com.homecare.dto.AppointmentReferralConvertRequest;
import com.homecare.dto.AppointmentReferralRequest;
import com.homecare.dto.AppointmentReferralResponse;
import com.homecare.dto.AppointmentReferralReviewRequest;
import com.homecare.entity.Appointment;
import com.homecare.entity.AppointmentReferral;
import com.homecare.entity.Client;
import com.homecare.entity.User;
import com.homecare.repository.AppointmentReferralRepository;
import com.homecare.repository.AppointmentRepository;
import com.homecare.repository.ClientCaregiverRepository;
import com.homecare.repository.ClientRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentReferralService {

    private final AppointmentReferralRepository referralRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClientCaregiverRepository clientCaregiverRepository;
    private final AuditLogService auditLogService;

    public AppointmentReferralService(
            AppointmentReferralRepository referralRepository,
            ClientRepository clientRepository,
            UserRepository userRepository,
            AppointmentRepository appointmentRepository,
            ClientCaregiverRepository clientCaregiverRepository,
            AuditLogService auditLogService
    ) {
        this.referralRepository = referralRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.clientCaregiverRepository = clientCaregiverRepository;
        this.auditLogService = auditLogService;
    }

    public AppointmentReferralResponse createReferral(AppointmentReferralRequest request) {
        User caregiver = userRepository.findById(request.getCaregiverId())
                .orElseThrow(() -> new RuntimeException("Caregiver not found."));

        Client client = null;

        if (request.getClientId() != null) {
            client = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client not found."));
        }

        AppointmentReferral referral = AppointmentReferral.builder()
                .client(client)
                .caregiver(caregiver)
                .clientFullName(request.getClientFullName())
                .clientPhone(request.getClientPhone())
                .clientEmail(request.getClientEmail())
                .clientAddress(request.getClientAddress())
                .referralSource(defaultValue(request.getReferralSource(), "HOSPITAL"))
                .hospitalName(request.getHospitalName())
                .dischargePlannerName(request.getDischargePlannerName())
                .dischargePlannerPhone(request.getDischargePlannerPhone())
                .requestedStartTime(request.getRequestedStartTime())
                .requestedEndTime(request.getRequestedEndTime())
                .serviceType(defaultValue(request.getServiceType(), "PERSONAL_CARE"))
                .notes(request.getNotes())
                .status("SUBMITTED")
                .build();

        AppointmentReferral savedReferral = referralRepository.save(referral);

        auditLogService.logAction(
                caregiver.getId(),
                caregiver.getFullName(),
                caregiver.getRole().name(),
                client != null ? client.getId() : null,
                "CREATE_APPOINTMENT_REFERRAL",
                "APPOINTMENT_REFERRAL",
                savedReferral.getId(),
                "Caregiver submitted appointment referral."
        );

        return mapToResponse(savedReferral);
    }

    public List<AppointmentReferralResponse> getAllReferrals() {
        return referralRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AppointmentReferralResponse> getReferralsByStatus(String status) {
        return referralRepository.findByStatusOrderByCreatedAtDesc(status.toUpperCase())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AppointmentReferralResponse> getReferralsByCaregiver(Long caregiverId) {
        return referralRepository.findByCaregiverIdOrderByCreatedAtDesc(caregiverId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AppointmentReferralResponse> getReferralsByClient(Long clientId) {
        return referralRepository.findByClientIdOrderByCreatedAtDesc(clientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AppointmentReferralResponse reviewReferral(
            Long referralId,
            AppointmentReferralReviewRequest request
    ) {
        AppointmentReferral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new RuntimeException("Appointment referral not found."));

        User reviewer = userRepository.findById(request.getReviewedByUserId())
                .orElseThrow(() -> new RuntimeException("Reviewer not found."));

        String status = defaultValue(request.getStatus(), "UNDER_REVIEW");

        if (!List.of("UNDER_REVIEW", "APPROVED", "REJECTED").contains(status)) {
            throw new RuntimeException("Invalid referral review status.");
        }

        referral.setStatus(status);
        referral.setAdminNotes(request.getAdminNotes());
        referral.setReviewedBy(reviewer);
        referral.setReviewedAt(LocalDateTime.now());

        AppointmentReferral savedReferral = referralRepository.save(referral);

        auditLogService.logAction(
                reviewer.getId(),
                reviewer.getFullName(),
                reviewer.getRole().name(),
                referral.getClient() != null ? referral.getClient().getId() : null,
                "REVIEW_APPOINTMENT_REFERRAL",
                "APPOINTMENT_REFERRAL",
                referral.getId(),
                "Referral marked as " + status + "."
        );

        return mapToResponse(savedReferral);
    }

    public AppointmentReferralResponse convertReferralToAppointment(
            Long referralId,
            AppointmentReferralConvertRequest request
    ) {
        AppointmentReferral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new RuntimeException("Appointment referral not found."));

        if ("CONVERTED".equalsIgnoreCase(referral.getStatus())) {
            throw new RuntimeException("Referral has already been converted.");
        }

        if (referral.getRequestedStartTime() == null || referral.getRequestedEndTime() == null) {
            throw new RuntimeException("Requested start and end time are required.");
        }

        if (!referral.getRequestedEndTime().isAfter(referral.getRequestedStartTime())) {
            throw new RuntimeException("Requested end time must be after start time.");
        }

        Long clientId = request.getClientId() != null
                ? request.getClientId()
                : referral.getClient() != null ? referral.getClient().getId() : null;

        if (clientId == null) {
            throw new RuntimeException("Client must be selected before converting referral.");
        }

        Long caregiverId = request.getCaregiverId() != null
                ? request.getCaregiverId()
                : referral.getCaregiver().getId();

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found."));

        User caregiver = userRepository.findById(caregiverId)
                .orElseThrow(() -> new RuntimeException("Caregiver not found."));

        User reviewer = userRepository.findById(request.getConvertedByUserId())
                .orElseThrow(() -> new RuntimeException("Reviewer not found."));

        boolean assigned = clientCaregiverRepository.existsByClientIdAndCaregiverIdAndActiveTrue(
                client.getId(),
                caregiver.getId()
        );

        if (!assigned) {
            throw new RuntimeException("Caregiver must be assigned to the client before conversion.");
        }

        validateNoScheduleConflict(
                client.getId(),
                caregiver.getId(),
                referral.getRequestedStartTime(),
                referral.getRequestedEndTime()
        );

        Appointment appointment = Appointment.builder()
                .client(client)
                .caregiver(caregiver)
                .startTime(referral.getRequestedStartTime())
                .endTime(referral.getRequestedEndTime())
                .serviceType(defaultValue(request.getServiceType(), referral.getServiceType()))
                .shiftType(defaultValue(request.getShiftType(), "REGULAR"))
                .status("SCHEDULED")
                .evvRequired(request.getEvvRequired() != null ? request.getEvvRequired() : true)
                .billable(request.getBillable() != null ? request.getBillable() : true)
                .repeatType("NONE")
                .notes(request.getNotes() != null ? request.getNotes() : referral.getNotes())
                .completed(false)
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);

        referral.setClient(client);
        referral.setCaregiver(caregiver);
        referral.setStatus("CONVERTED");
        referral.setConvertedAppointment(savedAppointment);
        referral.setReviewedBy(reviewer);
        referral.setReviewedAt(LocalDateTime.now());
        referral.setAdminNotes(
                request.getNotes() != null
                        ? request.getNotes()
                        : "Referral converted to official appointment."
        );

        AppointmentReferral savedReferral = referralRepository.save(referral);

        auditLogService.logAction(
                reviewer.getId(),
                reviewer.getFullName(),
                reviewer.getRole().name(),
                client.getId(),
                "CONVERT_REFERRAL_TO_APPOINTMENT",
                "APPOINTMENT_REFERRAL",
                referral.getId(),
                "Referral converted to appointment #" + savedAppointment.getId() + "."
        );

        auditLogService.logAction(
                reviewer.getId(),
                reviewer.getFullName(),
                reviewer.getRole().name(),
                client.getId(),
                "CREATE_APPOINTMENT_FROM_REFERRAL",
                "APPOINTMENT",
                savedAppointment.getId(),
                "Appointment created from referral #" + referral.getId() + "."
        );

        return mapToResponse(savedReferral);
    }

    private void validateNoScheduleConflict(
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
                .anyMatch(appointment -> !"CANCELLED".equalsIgnoreCase(appointment.getStatus()));

        if (caregiverConflict) {
            throw new RuntimeException("Caregiver already has an appointment during this time.");
        }

        boolean clientConflict = appointmentRepository
                .findByClientIdAndStartTimeLessThanAndEndTimeGreaterThan(
                        clientId,
                        endTime,
                        startTime
                )
                .stream()
                .anyMatch(appointment -> !"CANCELLED".equalsIgnoreCase(appointment.getStatus()));

        if (clientConflict) {
            throw new RuntimeException("Client already has an appointment during this time.");
        }
    }

    private String defaultValue(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value.toUpperCase();
    }

    private AppointmentReferralResponse mapToResponse(AppointmentReferral referral) {
        return AppointmentReferralResponse.builder()
                .id(referral.getId())
                .clientId(referral.getClient() != null ? referral.getClient().getId() : null)
                .clientName(referral.getClient() != null ? referral.getClient().getFullName() : null)
                .caregiverId(referral.getCaregiver().getId())
                .caregiverName(referral.getCaregiver().getFullName())
                .clientFullName(referral.getClientFullName())
                .clientPhone(referral.getClientPhone())
                .clientEmail(referral.getClientEmail())
                .clientAddress(referral.getClientAddress())
                .referralSource(referral.getReferralSource())
                .hospitalName(referral.getHospitalName())
                .dischargePlannerName(referral.getDischargePlannerName())
                .dischargePlannerPhone(referral.getDischargePlannerPhone())
                .requestedStartTime(referral.getRequestedStartTime())
                .requestedEndTime(referral.getRequestedEndTime())
                .serviceType(referral.getServiceType())
                .notes(referral.getNotes())
                .status(referral.getStatus())
                .adminNotes(referral.getAdminNotes())
                .convertedAppointmentId(
                        referral.getConvertedAppointment() != null
                                ? referral.getConvertedAppointment().getId()
                                : null
                )
                .reviewedAt(referral.getReviewedAt())
                .reviewedByName(
                        referral.getReviewedBy() != null
                                ? referral.getReviewedBy().getFullName()
                                : null
                )
                .createdAt(referral.getCreatedAt())
                .updatedAt(referral.getUpdatedAt())
                .build();
    }
}