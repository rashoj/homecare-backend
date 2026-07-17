package com.homecare.ai.detail;

import com.homecare.dto.AiCopilotDetailDTO;
import com.homecare.entity.Incident;
import com.homecare.repository.IncidentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class IncidentDetailService {

    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_UNDER_REVIEW = "UNDER_REVIEW";

    private static final String SEVERITY_HIGH = "HIGH";
    private static final String SEVERITY_CRITICAL = "CRITICAL";

    private final IncidentRepository incidentRepository;

    public IncidentDetailService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public List<AiCopilotDetailDTO> getActiveIncidents(Long organizationId) {
        return incidentRepository
                .findTop5ByOrganizationIdAndStatusInOrderByCreatedAtDesc(
                        organizationId,
                        activeStatuses()
                )
                .stream()
                .map(this::toDetailDTO)
                .toList();
    }

    public List<AiCopilotDetailDTO> getHighRiskIncidents(Long organizationId) {
        return incidentRepository
                .findTop5ByOrganizationIdAndSeverityInAndStatusInOrderByCreatedAtDesc(
                        organizationId,
                        List.of(
                                SEVERITY_HIGH,
                                SEVERITY_CRITICAL
                        ),
                        activeStatuses()
                )
                .stream()
                .map(this::toDetailDTO)
                .toList();
    }

    public List<AiCopilotDetailDTO> getStateReportableIncidents(
            Long organizationId
    ) {
        return incidentRepository
                .findTop5ByOrganizationIdAndStateReportableTrueAndStatusInOrderByCreatedAtDesc(
                        organizationId,
                        activeStatuses()
                )
                .stream()
                .map(this::toDetailDTO)
                .toList();
    }

    private List<String> activeStatuses() {
        return List.of(
                STATUS_SUBMITTED,
                STATUS_UNDER_REVIEW
        );
    }

    private AiCopilotDetailDTO toDetailDTO(Incident incident) {
        return new AiCopilotDetailDTO(
                incident.getClient() != null
                        ? incident.getClient().getFullName()
                        : "Unknown Client",

                valueOrDash(incident.getIncidentType()),

                valueOrDash(incident.getDescription()),

                "INCIDENT",

                valueOrDash(incident.getSeverity()),

                valueOrDash(incident.getStatus()),

                "/incidents",

                Map.of(
                        "Caregiver",
                        incident.getCaregiver() != null
                                ? incident.getCaregiver().getFullName()
                                : "Unknown Caregiver",

                        "Incident Date",
                        formatDateTime(incident.getIncidentDateTime()),

                        "State Reportable",
                        booleanLabel(incident.getStateReportable())
                )
        );
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank()
                ? "—"
                : value;
    }

    private String booleanLabel(Boolean value) {
        if (value == null) {
            return "—";
        }

        return value ? "Yes" : "No";
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "—";
        }

        return value.format(
                DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
        );
    }
}