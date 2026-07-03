package com.homecare.service;

import com.homecare.ai.dto.AppointmentInsightDTO;
import com.homecare.ai.dto.EVVInsightDTO;
import com.homecare.ai.dto.IncidentInsightDTO;
import com.homecare.ai.dto.OpenShiftInsightDTO;
import com.homecare.ai.insight.AppointmentInsightService;
import com.homecare.ai.insight.EVVInsightService;
import com.homecare.ai.insight.IncidentInsightService;
import com.homecare.ai.insight.OpenShiftInsightService;
import com.homecare.dto.AiCopilotActionDTO;
import com.homecare.dto.AiCopilotCardDTO;
import com.homecare.dto.AiCopilotResponse;
import com.homecare.entity.EVVException;
import com.homecare.entity.Incident;
import com.homecare.entity.OpenShift;
import com.homecare.repository.EVVExceptionRepository;
import com.homecare.repository.IncidentRepository;
import com.homecare.repository.OpenShiftRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AiCopilotService {

    private final AppointmentInsightService appointmentInsightService;
    private final OpenShiftInsightService openShiftInsightService;
    private final EVVInsightService evvInsightService;
    private final IncidentInsightService incidentInsightService;

    private final OpenShiftRepository openShiftRepository;
    private final EVVExceptionRepository evvExceptionRepository;
    private final IncidentRepository incidentRepository;

    private static final String STATUS_OPEN = "OPEN";
    private static final String SEVERITY_HIGH = "HIGH";
    private static final String SEVERITY_CRITICAL = "CRITICAL";

    public AiCopilotService(
            AppointmentInsightService appointmentInsightService,
            OpenShiftInsightService openShiftInsightService,
            EVVInsightService evvInsightService,
            IncidentInsightService incidentInsightService,
            OpenShiftRepository openShiftRepository,
            EVVExceptionRepository evvExceptionRepository,
            IncidentRepository incidentRepository
    ) {
        this.appointmentInsightService = appointmentInsightService;
        this.openShiftInsightService = openShiftInsightService;
        this.evvInsightService = evvInsightService;
        this.incidentInsightService = incidentInsightService;
        this.openShiftRepository = openShiftRepository;
        this.evvExceptionRepository = evvExceptionRepository;
        this.incidentRepository = incidentRepository;
    }

    public AiCopilotResponse answer(String message, Long organizationId) {
        if (message == null || message.isBlank()) {
            return defaultHelpResponse();
        }

        String normalized = message.trim().toLowerCase();

        if (containsAny(normalized, "open shift", "open shifts", "staffing", "coverage")) {
            return buildOpenShiftSummary(organizationId);
        }

        if (containsAny(normalized, "evv", "clock", "clock in", "clock out", "exception", "alert")) {
            return buildEVVSummary(organizationId);
        }

        if (containsAny(normalized, "incident", "incidents", "risk", "state reportable")) {
            return buildIncidentSummary(organizationId);
        }

        if (containsAny(normalized, "today", "summary", "agency", "operations", "dashboard")) {
            return buildTodaySummary(organizationId);
        }

        return defaultHelpResponse();
    }

    private AiCopilotResponse buildTodaySummary(Long organizationId) {
        AppointmentInsightDTO appointments = appointmentInsightService.getTodayInsight(organizationId);
        OpenShiftInsightDTO openShifts = openShiftInsightService.getInsight(organizationId);
        EVVInsightDTO evv = evvInsightService.getInsight(organizationId);
        IncidentInsightDTO incidents = incidentInsightService.getInsight(organizationId);

        String answer = """
                Today's CareBridge operations summary:

                Visits:
                • Total visits today: %d
                • Scheduled visits: %d
                • Completed visits: %d
                • Unassigned visits needing coverage: %d

                Staffing:
                • Open shifts available: %d

                EVV:
                • EVV exceptions needing review: %d
                • Unread EVV alerts: %d

                Incidents:
                • Submitted incidents: %d
                • High-risk incidents: %d
                • State-reportable incidents: %d

                Recommended focus:
                1. Review unassigned visits and open shifts.
                2. Clear unread EVV alerts and open EVV exceptions.
                3. Review high-risk or state-reportable incidents.
                """.formatted(
                appointments.totalToday(),
                appointments.scheduledToday(),
                appointments.completedToday(),
                appointments.unassignedToday(),
                openShifts.openShifts(),
                evv.openExceptions(),
                evv.unreadAlerts(),
                incidents.submittedIncidents(),
                incidents.highRiskIncidents(),
                incidents.stateReportableIncidents()
        );

        return new AiCopilotResponse(
                answer,
                List.of(
                        new AiCopilotCardDTO("Visits Today", String.valueOf(appointments.totalToday()), "Scheduled for today", "blue"),
                        new AiCopilotCardDTO("Open Shifts", String.valueOf(openShifts.openShifts()), "Needs coverage", openShifts.openShifts() > 0 ? "orange" : "green"),
                        new AiCopilotCardDTO("EVV Exceptions", String.valueOf(evv.openExceptions()), "Needs review", evv.openExceptions() > 0 ? "red" : "green"),
                        new AiCopilotCardDTO("High-Risk Incidents", String.valueOf(incidents.highRiskIncidents()), "Supervisor attention", incidents.highRiskIncidents() > 0 ? "red" : "green")
                ),
                List.of(
                        new AiCopilotActionDTO("Open Scheduler", "/scheduler"),
                        new AiCopilotActionDTO("Review EVV Exceptions", "/evv-exceptions"),
                        new AiCopilotActionDTO("Review Incidents", "/incidents")
                )
        );
    }

    private AiCopilotResponse buildOpenShiftSummary(Long organizationId) {
        OpenShiftInsightDTO insight = openShiftInsightService.getInsight(organizationId);

        List<OpenShift> topOpenShifts =
                openShiftRepository.findTop5ByOrganizationIdAndStatusOrderByStartTimeAsc(
                        organizationId,
                        STATUS_OPEN
                );

        String details = buildOpenShiftDetails(topOpenShifts);

        String answer = """
                Open shift and staffing summary:

                • Open shifts needing coverage: %d
                • Claimed shifts awaiting assignment: %d
                • Assigned open shifts: %d

                Upcoming open shifts:
                %s

                Recommended focus:
                1. Review open shifts needing coverage.
                2. Prioritize urgent and EVV-required shifts.
                3. Assign only eligible caregivers.
                """.formatted(
                insight.openShifts(),
                insight.claimedShifts(),
                insight.assignedShifts(),
                details
        );

        return new AiCopilotResponse(
                answer,
                List.of(
                        new AiCopilotCardDTO("Open Shifts", String.valueOf(insight.openShifts()), "Needs coverage", insight.openShifts() > 0 ? "orange" : "green"),
                        new AiCopilotCardDTO("Claimed", String.valueOf(insight.claimedShifts()), "Awaiting assignment", insight.claimedShifts() > 0 ? "blue" : "slate"),
                        new AiCopilotCardDTO("Assigned", String.valueOf(insight.assignedShifts()), "Covered shifts", "green")
                ),
                List.of(
                        new AiCopilotActionDTO("Open Shift Board", "/open-shifts"),
                        new AiCopilotActionDTO("Open Scheduler", "/scheduler")
                )
        );
    }

    private AiCopilotResponse buildEVVSummary(Long organizationId) {
        EVVInsightDTO insight = evvInsightService.getInsight(organizationId);

        List<EVVException> topExceptions =
                evvExceptionRepository.findTop5ByOrganizationIdAndStatusOrderByCreatedAtDesc(
                        organizationId,
                        STATUS_OPEN
                );

        String details = buildEVVExceptionDetails(topExceptions);

        String answer = """
                EVV review summary:

                • EVV exceptions needing review: %d
                • Reviewed exceptions: %d
                • Resolved exceptions: %d
                • High-severity exceptions: %d
                • Unread EVV alerts: %d

                Recent open EVV exceptions:
                %s

                Recommended focus:
                1. Review high-severity EVV exceptions first.
                2. Clear unread EVV alerts.
                3. Resolve verified missed clock-in or clock-out issues before payroll.
                """.formatted(
                insight.openExceptions(),
                insight.reviewedExceptions(),
                insight.resolvedExceptions(),
                insight.highSeverityExceptions(),
                insight.unreadAlerts(),
                details
        );

        return new AiCopilotResponse(
                answer,
                List.of(
                        new AiCopilotCardDTO("Open EVV Exceptions", String.valueOf(insight.openExceptions()), "Needs review", insight.openExceptions() > 0 ? "red" : "green"),
                        new AiCopilotCardDTO("Unread Alerts", String.valueOf(insight.unreadAlerts()), "EVV alert center", insight.unreadAlerts() > 0 ? "orange" : "green"),
                        new AiCopilotCardDTO("High Severity", String.valueOf(insight.highSeverityExceptions()), "Priority review", insight.highSeverityExceptions() > 0 ? "red" : "green")
                ),
                List.of(
                        new AiCopilotActionDTO("Review EVV Exceptions", "/evv-exceptions"),
                        new AiCopilotActionDTO("Open EVV Alerts", "/evv-alerts"),
                        new AiCopilotActionDTO("Open Clock Records", "/clock-records")
                )
        );
    }

    private AiCopilotResponse buildIncidentSummary(Long organizationId) {
        IncidentInsightDTO insight = incidentInsightService.getInsight(organizationId);

        List<Incident> topIncidents =
                incidentRepository.findTop5ByOrganizationIdAndSeverityInOrderByCreatedAtDesc(
                        organizationId,
                        List.of(SEVERITY_HIGH, SEVERITY_CRITICAL)
                );

        String details = buildIncidentDetails(topIncidents);

        String answer = """
                Incident review summary:

                • Submitted incidents: %d
                • Incidents under review: %d
                • High-risk incidents: %d
                • State-reportable incidents: %d

                Recent high-risk incidents:
                %s

                Recommended focus:
                1. Review state-reportable incidents immediately.
                2. Prioritize high-risk incidents.
                3. Add supervisor notes, corrective action, and follow-up before closing.
                """.formatted(
                insight.submittedIncidents(),
                insight.underReviewIncidents(),
                insight.highRiskIncidents(),
                insight.stateReportableIncidents(),
                details
        );

        return new AiCopilotResponse(
                answer,
                List.of(
                        new AiCopilotCardDTO("Submitted", String.valueOf(insight.submittedIncidents()), "Awaiting review", insight.submittedIncidents() > 0 ? "orange" : "green"),
                        new AiCopilotCardDTO("Under Review", String.valueOf(insight.underReviewIncidents()), "Active case review", "blue"),
                        new AiCopilotCardDTO("High Risk", String.valueOf(insight.highRiskIncidents()), "Priority incidents", insight.highRiskIncidents() > 0 ? "red" : "green"),
                        new AiCopilotCardDTO("State Reportable", String.valueOf(insight.stateReportableIncidents()), "Regulatory attention", insight.stateReportableIncidents() > 0 ? "red" : "green")
                ),
                List.of(
                        new AiCopilotActionDTO("Open Incidents", "/incidents")
                )
        );
    }

    private AiCopilotResponse defaultHelpResponse() {
        return new AiCopilotResponse(
                """
                I can help with:
                • Today's agency operations summary
                • Open shifts and staffing coverage
                • EVV exceptions and alerts
                • Incident and high-risk event review

                Try asking: "Summarize today's agency operations."
                """,
                List.of(),
                List.of(
                        new AiCopilotActionDTO("Open Dashboard", "/dashboard"),
                        new AiCopilotActionDTO("Open Scheduler", "/scheduler")
                )
        );
    }

    private String buildOpenShiftDetails(List<OpenShift> shifts) {
        if (shifts == null || shifts.isEmpty()) {
            return "No open shift details found.";
        }

        StringBuilder details = new StringBuilder();

        for (OpenShift shift : shifts) {
            details.append("• ")
                    .append(shift.getClient() != null ? shift.getClient().getFullName() : "Unknown client")
                    .append(" — ")
                    .append(formatDateTime(shift.getStartTime()))
                    .append(" to ")
                    .append(formatTime(shift.getEndTime()))
                    .append(" | Priority: ")
                    .append(valueOrDash(shift.getPriority()))
                    .append("\n");
        }

        return details.toString();
    }

    private String buildEVVExceptionDetails(List<EVVException> exceptions) {
        if (exceptions == null || exceptions.isEmpty()) {
            return "No open EVV exception details found.";
        }

        StringBuilder details = new StringBuilder();

        for (EVVException exception : exceptions) {
            details.append("• ")
                    .append(exception.getClient() != null ? exception.getClient().getFullName() : "Unknown client")
                    .append(" — ")
                    .append(valueOrDash(exception.getExceptionType()))
                    .append(" | Severity: ")
                    .append(valueOrDash(exception.getSeverity()))
                    .append("\n");
        }

        return details.toString();
    }

    private String buildIncidentDetails(List<Incident> incidents) {
        if (incidents == null || incidents.isEmpty()) {
            return "No high-risk incident details found.";
        }

        StringBuilder details = new StringBuilder();

        for (Incident incident : incidents) {
            details.append("• ")
                    .append(incident.getClient() != null ? incident.getClient().getFullName() : "Unknown client")
                    .append(" — ")
                    .append(valueOrDash(incident.getIncidentType()))
                    .append(" | Severity: ")
                    .append(valueOrDash(incident.getSeverity()))
                    .append(" | Date: ")
                    .append(formatDateTime(incident.getIncidentDateTime()))
                    .append("\n");
        }

        return details.toString();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) return "—";
        return value.format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a"));
    }

    private String formatTime(LocalDateTime value) {
        if (value == null) return "—";
        return value.format(DateTimeFormatter.ofPattern("h:mm a"));
    }
}