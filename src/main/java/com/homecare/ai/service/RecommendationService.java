package com.homecare.ai.service;

import com.homecare.ai.dto.*;
import com.homecare.ai.insight.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationService {

    private final AppointmentInsightService appointmentInsightService;
    private final OpenShiftInsightService openShiftInsightService;
    private final EVVInsightService evvInsightService;
    private final IncidentInsightService incidentInsightService;

    public RecommendationService(
            AppointmentInsightService appointmentInsightService,
            OpenShiftInsightService openShiftInsightService,
            EVVInsightService evvInsightService,
            IncidentInsightService incidentInsightService
    ) {
        this.appointmentInsightService = appointmentInsightService;
        this.openShiftInsightService = openShiftInsightService;
        this.evvInsightService = evvInsightService;
        this.incidentInsightService = incidentInsightService;
    }

    public List<RecommendationDTO> buildRecommendations(Long organizationId) {
        AppointmentInsightDTO appointments = appointmentInsightService.getTodayInsight(organizationId);
        OpenShiftInsightDTO openShifts = openShiftInsightService.getInsight(organizationId);
        EVVInsightDTO evv = evvInsightService.getInsight(organizationId);
        IncidentInsightDTO incidents = incidentInsightService.getInsight(organizationId);

        List<RecommendationDTO> recommendations = new ArrayList<>();

        if (appointments.unassignedToday() > 0) {
            recommendations.add(new RecommendationDTO(
                    "Assign Uncovered Visits",
                    appointments.unassignedToday() + " visits today do not have an assigned caregiver.",
                    "HIGH",
                    "/scheduler"
            ));
        }

        if (openShifts.openShifts() > 0) {
            recommendations.add(new RecommendationDTO(
                    "Fill Open Shifts",
                    openShifts.openShifts() + " open shifts need coverage.",
                    "HIGH",
                    "/open-shifts"
            ));
        }

        if (evv.highSeverityExceptions() > 0) {
            recommendations.add(new RecommendationDTO(
                    "Review High-Severity EVV",
                    evv.highSeverityExceptions() + " EVV exceptions are high severity.",
                    "HIGH",
                    "/evv-exceptions"
            ));
        }

        if (evv.unreadAlerts() > 0) {
            recommendations.add(new RecommendationDTO(
                    "Clear EVV Alerts",
                    evv.unreadAlerts() + " unread EVV alerts need review.",
                    "MEDIUM",
                    "/evv-alerts"
            ));
        }

        if (incidents.stateReportableIncidents() > 0) {
            recommendations.add(new RecommendationDTO(
                    "Review State-Reportable Incidents",
                    incidents.stateReportableIncidents() + " incidents may require regulatory follow-up.",
                    "URGENT",
                    "/incidents"
            ));
        }

        if (incidents.highRiskIncidents() > 0) {
            recommendations.add(new RecommendationDTO(
                    "Review High-Risk Incidents",
                    incidents.highRiskIncidents() + " incidents are marked high risk.",
                    "HIGH",
                    "/incidents"
            ));
        }

        if (recommendations.isEmpty()) {
            recommendations.add(new RecommendationDTO(
                    "Operations Look Healthy",
                    "No urgent operational issues were detected from today's live data.",
                    "LOW",
                    "/dashboard"
            ));
        }

        return recommendations;
    }
}