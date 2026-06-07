package com.homecare.service;

import com.homecare.dto.EVVAlertResponse;
import com.homecare.entity.EVVAlert;
import com.homecare.entity.EVVException;
import com.homecare.entity.Organization;
import com.homecare.entity.User;
import com.homecare.repository.EVVAlertRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EVVAlertService {

    private final EVVAlertRepository evvAlertRepository;
    private final UserRepository userRepository;

    public EVVAlertService(
            EVVAlertRepository evvAlertRepository,
            UserRepository userRepository
    ) {
        this.evvAlertRepository = evvAlertRepository;
        this.userRepository = userRepository;
    }

    public void createAlertFromException(EVVException exception) {
        EVVAlert alert = EVVAlert.builder()
                .exceptionId(exception.getId())
                .clientId(exception.getClient().getId())
                .caregiverId(exception.getCaregiver().getId())
                .appointmentId(exception.getAppointment().getId())
                .organization(exception.getOrganization())
                .alertType(exception.getExceptionType())
                .severity(exception.getSeverity())
                .status("UNREAD")
                .message(buildMessage(exception))
                .createdAt(LocalDateTime.now())
                .build();

        evvAlertRepository.save(alert);
    }

    public List<EVVAlertResponse> getAllAlerts(String actorEmail) {
        Organization organization = getOrganization(actorEmail);

        return evvAlertRepository
                .findByOrganizationIdOrderByCreatedAtDesc(organization.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<EVVAlertResponse> getUnreadAlerts(String actorEmail) {
        Organization organization = getOrganization(actorEmail);

        return evvAlertRepository
                .findByOrganizationIdAndStatusOrderByCreatedAtDesc(
                        organization.getId(),
                        "UNREAD"
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public EVVAlertResponse markAsRead(Long id, String actorEmail) {
        Organization organization = getOrganization(actorEmail);

        EVVAlert alert = evvAlertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("EVV alert not found."));

        if (alert.getOrganization() == null ||
                !alert.getOrganization().getId().equals(organization.getId())) {
            throw new RuntimeException("EVV alert not found for this organization.");
        }

        alert.setStatus("READ");
        alert.setReadAt(LocalDateTime.now());

        return mapToResponse(evvAlertRepository.save(alert));
    }

    private EVVAlertResponse mapToResponse(EVVAlert alert) {
        return EVVAlertResponse.builder()
                .id(alert.getId())
                .exceptionId(alert.getExceptionId())
                .clientId(alert.getClientId())
                .caregiverId(alert.getCaregiverId())
                .appointmentId(alert.getAppointmentId())
                .organizationId(
                        alert.getOrganization() != null
                                ? alert.getOrganization().getId()
                                : null
                )
                .alertType(alert.getAlertType())
                .severity(alert.getSeverity())
                .status(alert.getStatus())
                .message(alert.getMessage())
                .createdAt(alert.getCreatedAt())
                .readAt(alert.getReadAt())
                .build();
    }

    private Organization getOrganization(String actorEmail) {
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (actor.getOrganization() == null ||
                actor.getOrganization().getId() == null) {
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

    private String buildMessage(EVVException exception) {
        return exception.getExceptionType()
                + " detected for "
                + exception.getClient().getFullName()
                + " with caregiver "
                + exception.getCaregiver().getFullName()
                + ".";
    }
}