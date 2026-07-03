package com.homecare.ai.service;

import com.homecare.ai.dto.*;
import com.homecare.ai.insight.*;
import org.springframework.stereotype.Service;

@Service
public class AgencyHealthService {

    private final AppointmentInsightService appointmentInsightService;
    private final OpenShiftInsightService openShiftInsightService;
    private final EVVInsightService evvInsightService;
    private final IncidentInsightService incidentInsightService;

    public AgencyHealthService(
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

    public AgencyHealthDTO calculate(Long organizationId) {
        AppointmentInsightDTO appointments = appointmentInsightService.getTodayInsight(organizationId);
        OpenShiftInsightDTO openShifts = openShiftInsightService.getInsight(organizationId);
        EVVInsightDTO evv = evvInsightService.getInsight(organizationId);
        IncidentInsightDTO incidents = incidentInsightService.getInsight(organizationId);

        int score = 100;

        score -= Math.min(openShifts.openShifts() * 3, 20);
        score -= Math.min(appointments.unassignedToday() * 4, 20);
        score -= Math.min(evv.openExceptions() * 2, 20);
        score -= Math.min(evv.unreadAlerts(), 10);
        score -= Math.min(incidents.highRiskIncidents() * 5, 20);
        score -= Math.min(incidents.stateReportableIncidents() * 5, 20);

        score = Math.max(score, 0);

        if (score >= 90) {
            return new AgencyHealthDTO(score, "Excellent", "green");
        }

        if (score >= 75) {
            return new AgencyHealthDTO(score, "Good", "blue");
        }

        if (score >= 60) {
            return new AgencyHealthDTO(score, "Needs Attention", "orange");
        }

        return new AgencyHealthDTO(score, "Critical", "red");
    }
}