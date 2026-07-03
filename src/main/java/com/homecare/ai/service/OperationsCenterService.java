package com.homecare.ai.service;

import com.homecare.ai.dto.AIOperationsCenterDTO;
import com.homecare.ai.insight.AppointmentInsightService;
import com.homecare.ai.insight.EVVInsightService;
import com.homecare.ai.insight.IncidentInsightService;
import com.homecare.ai.insight.OpenShiftInsightService;
import org.springframework.stereotype.Service;

@Service
public class OperationsCenterService {

    private final AgencyHealthService agencyHealthService;
    private final RecommendationService recommendationService;
    private final AppointmentInsightService appointmentInsightService;
    private final OpenShiftInsightService openShiftInsightService;
    private final EVVInsightService evvInsightService;
    private final IncidentInsightService incidentInsightService;

    public OperationsCenterService(
            AgencyHealthService agencyHealthService,
            RecommendationService recommendationService,
            AppointmentInsightService appointmentInsightService,
            OpenShiftInsightService openShiftInsightService,
            EVVInsightService evvInsightService,
            IncidentInsightService incidentInsightService
    ) {
        this.agencyHealthService = agencyHealthService;
        this.recommendationService = recommendationService;
        this.appointmentInsightService = appointmentInsightService;
        this.openShiftInsightService = openShiftInsightService;
        this.evvInsightService = evvInsightService;
        this.incidentInsightService = incidentInsightService;
    }

    public AIOperationsCenterDTO getOperationsCenter(Long organizationId) {
        return new AIOperationsCenterDTO(
                agencyHealthService.calculate(organizationId),
                appointmentInsightService.getTodayInsight(organizationId),
                openShiftInsightService.getInsight(organizationId),
                evvInsightService.getInsight(organizationId),
                incidentInsightService.getInsight(organizationId),
                recommendationService.buildRecommendations(organizationId)
        );
    }
}