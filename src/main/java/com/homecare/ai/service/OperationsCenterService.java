package com.homecare.ai.service;

import com.homecare.ai.dto.AIOperationsCenterDTO;
import com.homecare.ai.dto.AgencyHealthDTO;
import com.homecare.ai.dto.AppointmentInsightDTO;
import com.homecare.ai.dto.EVVInsightDTO;
import com.homecare.ai.dto.IncidentInsightDTO;
import com.homecare.ai.dto.OpenShiftInsightDTO;
import com.homecare.ai.dto.RecommendationDTO;
import com.homecare.ai.insight.AppointmentInsightService;
import com.homecare.ai.insight.EVVInsightService;
import com.homecare.ai.insight.IncidentInsightService;
import com.homecare.ai.insight.OpenShiftInsightService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperationsCenterService {

    private final AgencyHealthService agencyHealthService;
    private final RecommendationService recommendationService;
    private final AppointmentInsightService appointmentInsightService;
    private final OpenShiftInsightService openShiftInsightService;
    private final EVVInsightService evvInsightService;
    private final IncidentInsightService incidentInsightService;
    private final OpenAIClientService openAIClientService;

    public OperationsCenterService(
            AgencyHealthService agencyHealthService,
            RecommendationService recommendationService,
            AppointmentInsightService appointmentInsightService,
            OpenShiftInsightService openShiftInsightService,
            EVVInsightService evvInsightService,
            IncidentInsightService incidentInsightService,
            OpenAIClientService openAIClientService
    ) {
        this.agencyHealthService = agencyHealthService;
        this.recommendationService = recommendationService;
        this.appointmentInsightService = appointmentInsightService;
        this.openShiftInsightService = openShiftInsightService;
        this.evvInsightService = evvInsightService;
        this.incidentInsightService = incidentInsightService;
        this.openAIClientService = openAIClientService;
    }

    public AIOperationsCenterDTO getOperationsCenter(Long organizationId) {
        AgencyHealthDTO health = agencyHealthService.calculate(organizationId);
        AppointmentInsightDTO appointments = appointmentInsightService.getTodayInsight(organizationId);
        OpenShiftInsightDTO openShifts = openShiftInsightService.getInsight(organizationId);
        EVVInsightDTO evv = evvInsightService.getInsight(organizationId);
        IncidentInsightDTO incidents = incidentInsightService.getInsight(organizationId);
        List<RecommendationDTO> recommendations =
                recommendationService.buildRecommendations(organizationId);

        String executiveBrief = generateExecutiveBriefSafely(
                health,
                appointments,
                openShifts,
                evv,
                incidents,
                recommendations
        );

        return new AIOperationsCenterDTO(
                health,
                appointments,
                openShifts,
                evv,
                incidents,
                recommendations,
                executiveBrief
        );
    }

    private String generateExecutiveBriefSafely(
            AgencyHealthDTO health,
            AppointmentInsightDTO appointments,
            OpenShiftInsightDTO openShifts,
            EVVInsightDTO evv,
            IncidentInsightDTO incidents,
            List<RecommendationDTO> recommendations
    ) {
        try {
            String prompt = buildExecutiveBriefPrompt(
                    health,
                    appointments,
                    openShifts,
                    evv,
                    incidents,
                    recommendations
            );

            return openAIClientService.generateExecutiveBrief(prompt);
        } catch (Exception error) {
            return buildFallbackBrief(health, appointments, openShifts, evv, incidents);
        }
    }

    private String buildExecutiveBriefPrompt(
            AgencyHealthDTO health,
            AppointmentInsightDTO appointments,
            OpenShiftInsightDTO openShifts,
            EVVInsightDTO evv,
            IncidentInsightDTO incidents,
            List<RecommendationDTO> recommendations
    ) {
        StringBuilder recommendationText = new StringBuilder();

        for (RecommendationDTO recommendation : recommendations) {
            recommendationText
                    .append("- ")
                    .append(recommendation.priority())
                    .append(": ")
                    .append(recommendation.title())
                    .append(" — ")
                    .append(recommendation.description())
                    .append("\n");
        }

        return """
                Generate a concise executive operations brief for a homecare agency supervisor.

                Use only the data below. Do not invent facts.

                Agency Health:
                Score: %d
                Label: %s
                Tone: %s

                Appointments:
                Total today: %d
                Scheduled today: %d
                Completed today: %d
                Unassigned today: %d

                Open Shifts:
                Open: %d
                Claimed: %d
                Assigned: %d

                EVV:
                Open exceptions: %d
                Reviewed exceptions: %d
                Resolved exceptions: %d
                High severity exceptions: %d
                Unread alerts: %d

                Incidents:
                Submitted: %d
                Under review: %d
                High risk: %d
                State reportable: %d

                Recommendations:
                %s

                Format:
                1 short paragraph.
                Then 3 bullet points titled "Top priorities".
                Keep it professional and concise.
                """.formatted(
                health.score(),
                health.label(),
                health.tone(),
                appointments.totalToday(),
                appointments.scheduledToday(),
                appointments.completedToday(),
                appointments.unassignedToday(),
                openShifts.openShifts(),
                openShifts.claimedShifts(),
                openShifts.assignedShifts(),
                evv.openExceptions(),
                evv.reviewedExceptions(),
                evv.resolvedExceptions(),
                evv.highSeverityExceptions(),
                evv.unreadAlerts(),
                incidents.submittedIncidents(),
                incidents.underReviewIncidents(),
                incidents.highRiskIncidents(),
                incidents.stateReportableIncidents(),
                recommendationText
        );
    }

    private String buildFallbackBrief(
            AgencyHealthDTO health,
            AppointmentInsightDTO appointments,
            OpenShiftInsightDTO openShifts,
            EVVInsightDTO evv,
            IncidentInsightDTO incidents
    ) {
        return """
                Agency health is currently %s with a score of %d. Today there are %d visits, %d open shifts, %d open EVV exceptions, and %d high-risk incidents.

                Top priorities:
                • Review high-severity EVV exceptions and unread alerts.
                • Address high-risk or state-reportable incidents.
                • Review staffing coverage and unassigned visits.
                """.formatted(
                health.label(),
                health.score(),
                appointments.totalToday(),
                openShifts.openShifts(),
                evv.openExceptions(),
                incidents.highRiskIncidents()
        );
    }
}