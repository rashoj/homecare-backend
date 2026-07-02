package com.homecare.service;

import com.homecare.dto.FraudAlertResponse;
import com.homecare.entity.*;
import com.homecare.repository.FraudAlertRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.homecare.dto.FraudSummaryResponse;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FraudAlertService {

    private final FraudAlertRepository fraudAlertRepository;
    private final UserRepository userRepository;

    public FraudAlertService(
            FraudAlertRepository fraudAlertRepository,
            UserRepository userRepository
    ) {
        this.fraudAlertRepository = fraudAlertRepository;
        this.userRepository = userRepository;
    }

    public FraudAlert createAlert(
            Organization organization,
            User caregiver,
            Client client,
            Long visitId,
            String alertType,
            String severity,
            Integer riskScore,
            String title,
            String description
    ) {
        FraudAlert alert = FraudAlert.builder()
                .organization(organization)
                .caregiver(caregiver)
                .client(client)
                .visitId(visitId)
                .alertType(alertType)
                .severity(severity)
                .riskScore(riskScore)
                .title(title)
                .description(description)
                .status("OPEN")
                .detectedAt(LocalDateTime.now())
                .build();

        return fraudAlertRepository.save(alert);
    }

    public List<FraudAlertResponse> getAlerts(String actorEmail) {
        User actor = getActor(actorEmail);

        return fraudAlertRepository
                .findByOrganizationIdOrderByDetectedAtDesc(
                        actor.getOrganization().getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<FraudAlertResponse> getOpenAlerts(String actorEmail) {
        User actor = getActor(actorEmail);

        return fraudAlertRepository
                .findByOrganizationIdAndStatusOrderByDetectedAtDesc(
                        actor.getOrganization().getId(),
                        "OPEN"
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public FraudAlertResponse resolveAlert(Long alertId, String actorEmail) {
        User actor = getActor(actorEmail);

        FraudAlert alert = fraudAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Fraud alert not found."));

        if (!alert.getOrganization().getId().equals(actor.getOrganization().getId())) {
            throw new RuntimeException("Fraud alert not found for this organization.");
        }

        alert.setStatus("RESOLVED");
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolvedBy(actor.getId());

        return mapToResponse(fraudAlertRepository.save(alert));
    }

    private FraudAlertResponse mapToResponse(FraudAlert alert) {
        return FraudAlertResponse.builder()
                .id(alert.getId())
                .alertType(alert.getAlertType())
                .severity(alert.getSeverity())
                .riskScore(alert.getRiskScore())
                .title(alert.getTitle())
                .description(alert.getDescription())
                .status(alert.getStatus())
                .detectedAt(alert.getDetectedAt())
                .caregiverId(alert.getCaregiver() != null ? alert.getCaregiver().getId() : null)
                .caregiverName(alert.getCaregiver() != null ? alert.getCaregiver().getFullName() : null)
                .clientId(alert.getClient() != null ? alert.getClient().getId() : null)
                .clientName(alert.getClient() != null ? alert.getClient().getFullName() : null)
                .visitId(alert.getVisitId())
                .build();
    }
    public FraudSummaryResponse getSummary(String actorEmail) {

        User actor = getActor(actorEmail);

        Long organizationId = actor.getOrganization().getId();

        long openAlerts =
                fraudAlertRepository.countByOrganizationIdAndStatus(
                        organizationId,
                        "OPEN"
                );

        long highAlerts =
                fraudAlertRepository.countByOrganizationIdAndSeverityAndStatus(
                        organizationId,
                        "HIGH",
                        "OPEN"
                );

        long criticalAlerts =
                fraudAlertRepository.countByOrganizationIdAndSeverityAndStatus(
                        organizationId,
                        "CRITICAL",
                        "OPEN"
                );

        int totalRiskScore =
                fraudAlertRepository
                        .findByOrganizationIdAndStatusOrderByDetectedAtDesc(
                                organizationId,
                                "OPEN"
                        )
                        .stream()
                        .mapToInt(a -> a.getRiskScore() != null
                                ? a.getRiskScore()
                                : 0)
                        .sum();

        return FraudSummaryResponse.builder()
                .openAlerts(openAlerts)
                .highAlerts(highAlerts)
                .criticalAlerts(criticalAlerts)
                .totalRiskScore(totalRiskScore)
                .build();
    }

    private User getActor(String actorEmail) {
        User actor = userRepository.findByEmailIgnoreCase(actorEmail)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found."));

        if (actor.getOrganization() == null ||
                actor.getOrganization().getId() == null) {
            throw new RuntimeException("User is not assigned to an organization.");
        }

        return actor;
    }
}