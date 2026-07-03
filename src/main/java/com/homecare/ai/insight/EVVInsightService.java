package com.homecare.ai.insight;

import com.homecare.ai.dto.EVVInsightDTO;
import com.homecare.repository.EVVAlertRepository;
import com.homecare.repository.EVVExceptionRepository;
import org.springframework.stereotype.Service;

@Service
public class EVVInsightService {

    private final EVVExceptionRepository evvExceptionRepository;
    private final EVVAlertRepository evvAlertRepository;

    public EVVInsightService(
            EVVExceptionRepository evvExceptionRepository,
            EVVAlertRepository evvAlertRepository
    ) {
        this.evvExceptionRepository = evvExceptionRepository;
        this.evvAlertRepository = evvAlertRepository;
    }

    public EVVInsightDTO getInsight(Long organizationId) {
        long highSeverity =
                evvExceptionRepository.countByOrganizationIdAndSeverity(organizationId, "HIGH")
                        + evvExceptionRepository.countByOrganizationIdAndSeverity(organizationId, "CRITICAL");

        return new EVVInsightDTO(
                evvExceptionRepository.countByOrganizationIdAndStatus(organizationId, "OPEN"),
                evvExceptionRepository.countByOrganizationIdAndStatus(organizationId, "REVIEWED"),
                evvExceptionRepository.countByOrganizationIdAndStatus(organizationId, "RESOLVED"),
                highSeverity,
                evvAlertRepository.countByOrganizationIdAndStatus(organizationId, "UNREAD")
        );
    }
}