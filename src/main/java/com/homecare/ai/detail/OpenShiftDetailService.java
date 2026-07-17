package com.homecare.ai.detail;

import com.homecare.dto.AiCopilotDetailDTO;
import com.homecare.entity.OpenShift;
import com.homecare.repository.OpenShiftRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class OpenShiftDetailService {

    private static final String STATUS_OPEN = "OPEN";

    private static final String PRIORITY_HIGH = "HIGH";
    private static final String PRIORITY_URGENT = "URGENT";

    private final OpenShiftRepository openShiftRepository;

    public OpenShiftDetailService(OpenShiftRepository openShiftRepository) {
        this.openShiftRepository = openShiftRepository;
    }

    public List<AiCopilotDetailDTO> getOpenShifts(Long organizationId) {
        return openShiftRepository
                .findTop5ByOrganizationIdAndStatusOrderByStartTimeAsc(
                        organizationId,
                        STATUS_OPEN
                )
                .stream()
                .map(this::toDetailDTO)
                .toList();
    }

    public List<AiCopilotDetailDTO> getPriorityOpenShifts(Long organizationId) {
        return openShiftRepository
                .findTop5ByOrganizationIdAndStatusAndPriorityInOrderByStartTimeAsc(
                        organizationId,
                        STATUS_OPEN,
                        List.of(PRIORITY_HIGH, PRIORITY_URGENT)
                )
                .stream()
                .map(this::toDetailDTO)
                .toList();
    }

    private AiCopilotDetailDTO toDetailDTO(OpenShift shift) {
        return new AiCopilotDetailDTO(
                shift.getClient() != null
                        ? shift.getClient().getFullName()
                        : "Unknown Client",
                valueOrDash(shift.getServiceType()),
                valueOrDash(shift.getNotes()),
                "OPEN_SHIFT",
                valueOrDash(shift.getPriority()),
                valueOrDash(shift.getStatus()),
                "/open-shifts",
                Map.of(
                        "Start",
                        formatDateTime(shift.getStartTime()),

                        "End",
                        formatDateTime(shift.getEndTime()),

                        "Shift Type",
                        valueOrDash(shift.getShiftType()),

                        "Required Skills",
                        valueOrDash(shift.getRequiredSkills()),

                        "EVV Required",
                        booleanLabel(shift.getEvvRequired()),

                        "Billable",
                        booleanLabel(shift.getBillable()),

                        "Expires",
                        formatDateTime(shift.getExpiresAt())
                )
        );
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
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