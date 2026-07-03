package com.homecare.ai.insight;

import com.homecare.ai.dto.OpenShiftInsightDTO;
import com.homecare.repository.OpenShiftRepository;
import org.springframework.stereotype.Service;

@Service
public class OpenShiftInsightService {

    private final OpenShiftRepository openShiftRepository;

    public OpenShiftInsightService(OpenShiftRepository openShiftRepository) {
        this.openShiftRepository = openShiftRepository;
    }

    public OpenShiftInsightDTO getInsight(Long organizationId) {
        return new OpenShiftInsightDTO(
                openShiftRepository.countByOrganizationIdAndStatus(organizationId, "OPEN"),
                openShiftRepository.countByOrganizationIdAndStatus(organizationId, "CLAIMED"),
                openShiftRepository.countByOrganizationIdAndStatus(organizationId, "ASSIGNED")
        );
    }
}