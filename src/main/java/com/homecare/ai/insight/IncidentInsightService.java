package com.homecare.ai.insight;

import com.homecare.ai.dto.IncidentInsightDTO;
import com.homecare.repository.IncidentRepository;
import org.springframework.stereotype.Service;

@Service
public class IncidentInsightService {

    private final IncidentRepository incidentRepository;

    public IncidentInsightService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public IncidentInsightDTO getInsight(Long organizationId) {
        long highRisk =
                incidentRepository.countByOrganizationIdAndSeverity(organizationId, "HIGH")
                        + incidentRepository.countByOrganizationIdAndSeverity(organizationId, "CRITICAL");

        return new IncidentInsightDTO(
                incidentRepository.countByOrganizationIdAndStatus(organizationId, "SUBMITTED"),
                incidentRepository.countByOrganizationIdAndStatus(organizationId, "UNDER_REVIEW"),
                highRisk,
                incidentRepository.countByOrganizationIdAndStateReportableTrue(organizationId)
        );
    }
}