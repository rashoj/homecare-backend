package com.homecare.ai.detail;

import com.homecare.dto.AiCopilotDetailDTO;
import com.homecare.entity.EVVException;
import com.homecare.repository.EVVExceptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class EVVDetailService {

    private static final String STATUS_OPEN = "OPEN";
    private static final String SEVERITY_HIGH = "HIGH";
    private static final String SEVERITY_CRITICAL = "CRITICAL";

    private final EVVExceptionRepository evvExceptionRepository;

    public EVVDetailService(EVVExceptionRepository evvExceptionRepository) {
        this.evvExceptionRepository = evvExceptionRepository;
    }

    public List<AiCopilotDetailDTO> getOpenExceptions(Long organizationId) {
        return evvExceptionRepository
                .findTop5ByOrganizationIdAndStatusOrderByCreatedAtDesc(
                        organizationId,
                        STATUS_OPEN
                )
                .stream()
                .map(this::toDetailDTO)
                .toList();
    }

    public List<AiCopilotDetailDTO> getHighSeverityOpenExceptions(
            Long organizationId
    ) {
        return evvExceptionRepository
                .findTop5ByOrganizationIdAndSeverityInAndStatusOrderByCreatedAtDesc(
                        organizationId,
                        List.of(SEVERITY_HIGH, SEVERITY_CRITICAL),
                        STATUS_OPEN
                )
                .stream()
                .map(this::toDetailDTO)
                .toList();
    }

    private AiCopilotDetailDTO toDetailDTO(EVVException exception) {
        return new AiCopilotDetailDTO(
                exception.getClient() != null
                        ? exception.getClient().getFullName()
                        : "Unknown Client",
                valueOrDash(exception.getExceptionType()),
                valueOrDash(exception.getDescription()),
                "EVV",
                valueOrDash(exception.getSeverity()),
                valueOrDash(exception.getStatus()),
                "/evv-exceptions",
                Map.of(
                        "Caregiver",
                        exception.getCaregiver() != null
                                ? exception.getCaregiver().getFullName()
                                : "Unknown Caregiver",

                        "Created",
                        formatDateTime(exception.getCreatedAt()),

                        "Appointment",
                        exception.getAppointment() != null
                                && exception.getAppointment().getId() != null
                                ? String.valueOf(exception.getAppointment().getId())
                                : "—"
                )
        );
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
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