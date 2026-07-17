package com.homecare.ai.insight;

import com.homecare.ai.dto.IncidentInsightDTO;
import com.homecare.repository.IncidentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncidentInsightService {

    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_UNDER_REVIEW = "UNDER_REVIEW";

    private static final String SEVERITY_HIGH = "HIGH";
    private static final String SEVERITY_CRITICAL = "CRITICAL";

    private final IncidentRepository incidentRepository;

    public IncidentInsightService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public IncidentInsightDTO getInsight(Long organizationId) {
        List<String> activeStatuses = List.of(
                STATUS_SUBMITTED,
                STATUS_UNDER_REVIEW
        );

        long highRisk =
                incidentRepository.countByOrganizationIdAndSeverityAndStatusIn(
                        organizationId,
                        SEVERITY_HIGH,
                        activeStatuses
                )
                        + incidentRepository.countByOrganizationIdAndSeverityAndStatusIn(
                        organizationId,
                        SEVERITY_CRITICAL,
                        activeStatuses
                );

        return new IncidentInsightDTO(
                incidentRepository.countByOrganizationIdAndStatus(
                        organizationId,
                        STATUS_SUBMITTED
                ),
                incidentRepository.countByOrganizationIdAndStatus(
                        organizationId,
                        STATUS_UNDER_REVIEW
                ),
                highRisk,
                incidentRepository.countByOrganizationIdAndStateReportableTrueAndStatusIn(
                        organizationId,
                        activeStatuses
                )
        );
    }
}