package com.homecare.service;

import com.homecare.ai.detail.EVVDetailService;
import com.homecare.ai.detail.OpenShiftDetailService;
import com.homecare.ai.dto.AppointmentInsightDTO;
import com.homecare.ai.dto.EVVInsightDTO;
import com.homecare.ai.dto.IncidentInsightDTO;
import com.homecare.ai.dto.OpenShiftInsightDTO;
import com.homecare.ai.insight.AppointmentInsightService;
import com.homecare.ai.insight.EVVInsightService;
import com.homecare.ai.insight.IncidentInsightService;
import com.homecare.ai.insight.OpenShiftInsightService;
import com.homecare.ai.intent.AiIntentResult;
import com.homecare.ai.intent.AiIntentService;
import com.homecare.dto.AiCopilotActionDTO;
import com.homecare.dto.AiCopilotCardDTO;
import com.homecare.dto.AiCopilotDetailDTO;
import com.homecare.dto.AiCopilotResponse;
import com.homecare.entity.Incident;
import com.homecare.repository.IncidentRepository;
import com.homecare.ai.detail.IncidentDetailService;
import com.homecare.ai.detail.ClientDetailService;
import com.homecare.ai.intent.ClientNameExtractor;
import com.homecare.ai.detail.CaregiverDetailService;
import com.homecare.ai.intent.CaregiverNameExtractor;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AiCopilotService {

    private static final String SEVERITY_HIGH = "HIGH";
    private static final String SEVERITY_CRITICAL = "CRITICAL";

    private final AiIntentService aiIntentService;
    private final AppointmentInsightService appointmentInsightService;
    private final OpenShiftInsightService openShiftInsightService;
    private final EVVInsightService evvInsightService;
    private final IncidentInsightService incidentInsightService;
    private final EVVDetailService evvDetailService;
    private final OpenShiftDetailService openShiftDetailService;
    private final IncidentRepository incidentRepository;
    private final IncidentDetailService incidentDetailService;
    private final ClientDetailService clientDetailService;
    private final ClientNameExtractor clientNameExtractor;
    private final CaregiverDetailService caregiverDetailService;
    private final CaregiverNameExtractor caregiverNameExtractor;

    public AiCopilotService(
            AiIntentService aiIntentService,
            AppointmentInsightService appointmentInsightService,
            OpenShiftInsightService openShiftInsightService,
            EVVInsightService evvInsightService,
            IncidentInsightService incidentInsightService,
            EVVDetailService evvDetailService,
            OpenShiftDetailService openShiftDetailService,
            IncidentRepository incidentRepository,
            IncidentDetailService incidentDetailService,
            ClientDetailService clientDetailService,
            ClientNameExtractor clientNameExtractor,
            CaregiverDetailService caregiverDetailService,
            CaregiverNameExtractor caregiverNameExtractor
    ) {
        this.aiIntentService = aiIntentService;
        this.appointmentInsightService = appointmentInsightService;
        this.openShiftInsightService = openShiftInsightService;
        this.evvInsightService = evvInsightService;
        this.incidentInsightService = incidentInsightService;
        this.evvDetailService = evvDetailService;
        this.openShiftDetailService = openShiftDetailService;
        this.incidentRepository = incidentRepository;
        this.incidentDetailService = incidentDetailService;
        this.clientDetailService = clientDetailService;
        this.clientNameExtractor = clientNameExtractor;
        this.caregiverDetailService = caregiverDetailService;
        this.caregiverNameExtractor = caregiverNameExtractor;
    }

    public AiCopilotResponse answer(String message, Long organizationId) {
        AiIntentResult intentResult = aiIntentService.detect(message);

        return switch (intentResult.intent()) {
            case TODAY_SUMMARY, DASHBOARD ->
                    buildTodaySummary(organizationId);

            case OPEN_SHIFTS ->
                    buildOpenShiftSummary(organizationId);

            case OPEN_SHIFT_PRIORITY ->
                    buildPriorityOpenShiftSummary(organizationId);

            case INCIDENT_REVIEW ->
                    buildIncidentSummary(organizationId);

            case INCIDENT_HIGH_RISK ->
                    buildHighRiskIncidentSummary(organizationId);

            case INCIDENT_STATE_REPORTABLE ->
                    buildStateReportableIncidentSummary(organizationId);

            case EVV_REVIEW ->
                    buildEVVSummary(organizationId);

            case EVV_HIGH_SEVERITY ->
                    buildHighSeverityEVVSummary(organizationId);


            case CLIENT_ACTIVE ->
                    buildActiveClientSummary(organizationId);

            case CLIENT_LOOKUP ->
                    buildClientLookup(
                            organizationId,
                            intentResult.originalMessage()
                    );

            case CLIENT_WITH_EVV_ISSUES ->
                    buildClientsWithEVVIssues(organizationId);

            case CLIENT_WITH_INCIDENTS ->
                    buildClientsWithIncidents(organizationId);

            case CAREGIVER_LOOKUP ->
                    buildCaregiverLookup(
                            organizationId,
                            intentResult.originalMessage()
                    );

            case CAREGIVER_ACTIVE ->
                    buildActiveCaregiverSummary(organizationId);

            case CAREGIVER_WITH_EVV_ISSUES ->
                    buildCaregiversWithEVVIssues(organizationId);

            case CAREGIVER_WITH_HIGH_SEVERITY_EVV ->
                    buildCaregiversWithHighSeverityEVV(organizationId);

            case UNKNOWN ->
                resolveUnknownIntent(
                        organizationId,
                        intentResult.originalMessage()

                );

        };
    }

    private AiCopilotResponse buildTodaySummary(Long organizationId) {
        AppointmentInsightDTO appointments =
                appointmentInsightService.getTodayInsight(organizationId);

        OpenShiftInsightDTO openShifts =
                openShiftInsightService.getInsight(organizationId);

        EVVInsightDTO evv =
                evvInsightService.getInsight(organizationId);

        IncidentInsightDTO incidents =
                incidentInsightService.getInsight(organizationId);

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
                        new AiCopilotCardDTO(
                                "Visits Today",
                                String.valueOf(appointments.totalToday()),
                                "Scheduled for today",
                                "blue"
                        ),
                        new AiCopilotCardDTO(
                                "Open Shifts",
                                String.valueOf(openShifts.openShifts()),
                                "Needs coverage",
                                openShifts.openShifts() > 0 ? "orange" : "green"
                        ),
                        new AiCopilotCardDTO(
                                "EVV Exceptions",
                                String.valueOf(evv.openExceptions()),
                                "Needs review",
                                evv.openExceptions() > 0 ? "red" : "green"
                        ),
                        new AiCopilotCardDTO(
                                "High-Risk Incidents",
                                String.valueOf(incidents.highRiskIncidents()),
                                "Supervisor attention",
                                incidents.highRiskIncidents() > 0 ? "red" : "green"
                        )
                ),
                List.of(
                        new AiCopilotActionDTO("Open Scheduler", "/scheduler"),
                        new AiCopilotActionDTO("Review EVV Exceptions", "/evv-exceptions"),
                        new AiCopilotActionDTO("Review Incidents", "/incidents")
                )
        );
    }

    private AiCopilotResponse buildOpenShiftSummary(Long organizationId) {
        OpenShiftInsightDTO insight =
                openShiftInsightService.getInsight(organizationId);

        List<AiCopilotDetailDTO> detailCards =
                openShiftDetailService.getOpenShifts(organizationId);

        String answer;

        if (detailCards.isEmpty()) {
            answer = """
                    No open shifts currently need coverage.

                    Staffing summary:
                    • Open shifts: %d
                    • Claimed shifts: %d
                    • Assigned shifts: %d
                    """.formatted(
                    insight.openShifts(),
                    insight.claimedShifts(),
                    insight.assignedShifts()
            );
        } else {
            answer = """
                    Open shift and staffing summary:

                    • Open shifts needing coverage: %d
                    • Claimed shifts awaiting assignment: %d
                    • Assigned open shifts: %d
                    • Showing the next %d open shifts.

                    Review the priority records below for shift timing, required skills, EVV requirements, and expiration.
                    """.formatted(
                    insight.openShifts(),
                    insight.claimedShifts(),
                    insight.assignedShifts(),
                    detailCards.size()
            );
        }

        return new AiCopilotResponse(
                answer,
                List.of(
                        new AiCopilotCardDTO(
                                "Open Shifts",
                                String.valueOf(insight.openShifts()),
                                "Needs coverage",
                                insight.openShifts() > 0 ? "orange" : "green"
                        ),
                        new AiCopilotCardDTO(
                                "Claimed",
                                String.valueOf(insight.claimedShifts()),
                                "Awaiting assignment",
                                insight.claimedShifts() > 0 ? "blue" : "slate"
                        ),
                        new AiCopilotCardDTO(
                                "Assigned",
                                String.valueOf(insight.assignedShifts()),
                                "Covered shifts",
                                "green"
                        )
                ),
                List.of(
                        new AiCopilotActionDTO("Open Shift Board", "/open-shifts"),
                        new AiCopilotActionDTO("Open Scheduler", "/scheduler")
                ),
                detailCards,
                "OPEN_SHIFT",
                insight.openShifts() > 0 ? "HIGH" : "NORMAL"
        );
    }

    private AiCopilotResponse buildPriorityOpenShiftSummary(Long organizationId) {
        OpenShiftInsightDTO insight =
                openShiftInsightService.getInsight(organizationId);

        List<AiCopilotDetailDTO> details =
                openShiftDetailService.getPriorityOpenShifts(organizationId);

        String answer;

        if (details.isEmpty()) {
            answer = """
                    No open urgent or high-priority shifts were found.

                    Current staffing summary:
                    • Open shifts: %d
                    • Claimed shifts: %d
                    • Assigned shifts: %d
                    """.formatted(
                    insight.openShifts(),
                    insight.claimedShifts(),
                    insight.assignedShifts()
            );
        } else {
            answer = """
                    Priority staffing review:

                    • Open shifts requiring coverage: %d
                    • Showing %d urgent or high-priority open shifts.

                    Review these shifts and assign eligible caregivers based on availability and required skills.
                    """.formatted(
                    insight.openShifts(),
                    details.size()
            );
        }

        return new AiCopilotResponse(
                answer,
                List.of(
                        new AiCopilotCardDTO(
                                "Priority Shifts",
                                String.valueOf(details.size()),
                                "Urgent or high priority",
                                details.isEmpty() ? "green" : "red"
                        ),
                        new AiCopilotCardDTO(
                                "All Open Shifts",
                                String.valueOf(insight.openShifts()),
                                "Needs coverage",
                                insight.openShifts() > 0 ? "orange" : "green"
                        ),
                        new AiCopilotCardDTO(
                                "Claimed",
                                String.valueOf(insight.claimedShifts()),
                                "Awaiting assignment",
                                insight.claimedShifts() > 0 ? "blue" : "slate"
                        )
                ),
                List.of(
                        new AiCopilotActionDTO("Open Shift Board", "/open-shifts"),
                        new AiCopilotActionDTO("Open Scheduler", "/scheduler")
                ),
                details,
                "OPEN_SHIFT",
                details.isEmpty() ? "NORMAL" : "HIGH"
        );
    }

    private AiCopilotResponse buildEVVSummary(Long organizationId) {
        EVVInsightDTO insight =
                evvInsightService.getInsight(organizationId);

        List<AiCopilotDetailDTO> detailCards =
                evvDetailService.getOpenExceptions(organizationId);

        String answer;

        if (detailCards.isEmpty()) {
            answer = """
                    No open EVV exceptions were found.

                    Current EVV summary:
                    • Open exceptions: %d
                    • Reviewed exceptions: %d
                    • Resolved exceptions: %d
                    • High-severity exceptions: %d
                    • Unread alerts: %d
                    """.formatted(
                    insight.openExceptions(),
                    insight.reviewedExceptions(),
                    insight.resolvedExceptions(),
                    insight.highSeverityExceptions(),
                    insight.unreadAlerts()
            );
        } else {
            answer = """
                    EVV review summary:

                    • EVV exceptions needing review: %d
                    • Reviewed exceptions: %d
                    • Resolved exceptions: %d
                    • High-severity exceptions: %d
                    • Unread EVV alerts: %d
                    • Showing the most recent %d open EVV exceptions.

                    Review high-severity exceptions before payroll or billing workflows continue.
                    """.formatted(
                    insight.openExceptions(),
                    insight.reviewedExceptions(),
                    insight.resolvedExceptions(),
                    insight.highSeverityExceptions(),
                    insight.unreadAlerts(),
                    detailCards.size()
            );
        }

        return new AiCopilotResponse(
                answer,
                List.of(
                        new AiCopilotCardDTO(
                                "Open EVV Exceptions",
                                String.valueOf(insight.openExceptions()),
                                "Needs review",
                                insight.openExceptions() > 0 ? "red" : "green"
                        ),
                        new AiCopilotCardDTO(
                                "Unread Alerts",
                                String.valueOf(insight.unreadAlerts()),
                                "EVV alert center",
                                insight.unreadAlerts() > 0 ? "orange" : "green"
                        ),
                        new AiCopilotCardDTO(
                                "High Severity",
                                String.valueOf(insight.highSeverityExceptions()),
                                "Priority review",
                                insight.highSeverityExceptions() > 0 ? "red" : "green"
                        )
                ),
                List.of(
                        new AiCopilotActionDTO("Review EVV Exceptions", "/evv-exceptions"),
                        new AiCopilotActionDTO("Open EVV Alerts", "/evv-alerts"),
                        new AiCopilotActionDTO("Open Clock Records", "/clock-records")
                ),
                detailCards,
                "EVV",
                insight.highSeverityExceptions() > 0 ? "HIGH" : "NORMAL"
        );
    }

    private AiCopilotResponse buildHighSeverityEVVSummary(Long organizationId) {
        EVVInsightDTO insight =
                evvInsightService.getInsight(organizationId);

        List<AiCopilotDetailDTO> details =
                evvDetailService.getHighSeverityOpenExceptions(organizationId);

        String answer;

        if (details.isEmpty()) {
            answer = """
                    No open high-severity EVV exceptions were found.

                    Current EVV summary:
                    • Open exceptions: %d
                    • High-severity exceptions: %d
                    • Unread alerts: %d
                    """.formatted(
                    insight.openExceptions(),
                    insight.highSeverityExceptions(),
                    insight.unreadAlerts()
            );
        } else {
            answer = """
                    High-severity EVV exception review:

                    • Total high-severity open exceptions: %d
                    • Showing the most recent %d high-severity open exceptions.

                    Review these records before payroll or billing workflows continue.
                    """.formatted(
                    insight.highSeverityExceptions(),
                    details.size()
            );
        }

        return new AiCopilotResponse(
                answer,
                List.of(
                        new AiCopilotCardDTO(
                                "High Severity",
                                String.valueOf(insight.highSeverityExceptions()),
                                "Priority EVV review",
                                insight.highSeverityExceptions() > 0 ? "red" : "green"
                        ),
                        new AiCopilotCardDTO(
                                "Open Exceptions",
                                String.valueOf(insight.openExceptions()),
                                "Open EVV records",
                                insight.openExceptions() > 0 ? "orange" : "green"
                        ),
                        new AiCopilotCardDTO(
                                "Unread Alerts",
                                String.valueOf(insight.unreadAlerts()),
                                "EVV alert center",
                                insight.unreadAlerts() > 0 ? "orange" : "green"
                        )
                ),
                List.of(
                        new AiCopilotActionDTO("Review EVV Exceptions", "/evv-exceptions"),
                        new AiCopilotActionDTO("Open Clock Records", "/clock-records")
                ),
                details,
                "EVV",
                insight.highSeverityExceptions() > 0 ? "HIGH" : "NORMAL"
        );
    }

    private AiCopilotResponse buildIncidentSummary(Long organizationId) {
        IncidentInsightDTO insight =
                incidentInsightService.getInsight(organizationId);

        List<AiCopilotDetailDTO> details =
                incidentDetailService.getActiveIncidents(organizationId);

        String answer;

        if (details.isEmpty()) {
            answer = """
                No active incidents currently require review.

                Incident summary:
                • Submitted incidents: %d
                • Incidents under review: %d
                • High-risk incidents: %d
                • State-reportable incidents: %d
                """.formatted(
                    insight.submittedIncidents(),
                    insight.underReviewIncidents(),
                    insight.highRiskIncidents(),
                    insight.stateReportableIncidents()
            );
        } else {
            answer = """
                Incident review summary:

                • Submitted incidents: %d
                • Incidents under review: %d
                • High-risk incidents: %d
                • State-reportable incidents: %d
                • Showing the most recent %d active incidents.

                Review priority records below and complete required supervisor follow-up.
                """.formatted(
                    insight.submittedIncidents(),
                    insight.underReviewIncidents(),
                    insight.highRiskIncidents(),
                    insight.stateReportableIncidents(),
                    details.size()
            );
        }

        return new AiCopilotResponse(
                answer,
                List.of(
                        new AiCopilotCardDTO(
                                "Submitted",
                                String.valueOf(insight.submittedIncidents()),
                                "Awaiting review",
                                insight.submittedIncidents() > 0
                                        ? "orange"
                                        : "green"
                        ),
                        new AiCopilotCardDTO(
                                "Under Review",
                                String.valueOf(insight.underReviewIncidents()),
                                "Active case review",
                                "blue"
                        ),
                        new AiCopilotCardDTO(
                                "High Risk",
                                String.valueOf(insight.highRiskIncidents()),
                                "Active priority incidents",
                                insight.highRiskIncidents() > 0
                                        ? "red"
                                        : "green"
                        ),
                        new AiCopilotCardDTO(
                                "State Reportable",
                                String.valueOf(insight.stateReportableIncidents()),
                                "Active regulatory attention",
                                insight.stateReportableIncidents() > 0
                                        ? "red"
                                        : "green"
                        )
                ),
                List.of(
                        new AiCopilotActionDTO(
                                "Open Incidents",
                                "/incidents"
                        )
                ),
                details,
                "INCIDENT",
                determineIncidentSeverity(insight)
        );
    }
    private AiCopilotResponse defaultHelpResponse() {
        return new AiCopilotResponse(
                """
                I can help with:
                • Today's agency operations summary
                • Open shifts and staffing coverage
                • Urgent or high-priority open shifts
                • EVV exceptions and alerts
                • High-severity EVV exceptions
                • Incident and high-risk event review

                Try asking: "Which open shifts are urgent or high priority?"
                """,
                List.of(),
                List.of(
                        new AiCopilotActionDTO("Open Dashboard", "/dashboard"),
                        new AiCopilotActionDTO("Open Scheduler", "/scheduler")
                )
        );
    }

    private String buildIncidentDetails(List<Incident> incidents) {
        if (incidents == null || incidents.isEmpty()) {
            return "No high-risk incident details found.";
        }

        StringBuilder details = new StringBuilder();

        for (Incident incident : incidents) {
            details.append("• ")
                    .append(
                            incident.getClient() != null
                                    ? incident.getClient().getFullName()
                                    : "Unknown client"
                    )
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

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "—";
        }

        return value.format(
                DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
        );
    }
    private String determineIncidentSeverity(IncidentInsightDTO insight) {
        if (insight.stateReportableIncidents() > 0) {
            return "CRITICAL";
        }

        if (insight.highRiskIncidents() > 0) {
            return "HIGH";
        }

        if (
                insight.submittedIncidents() > 0
                        || insight.underReviewIncidents() > 0
        ) {
            return "MEDIUM";
        }

        return "NORMAL";
    }
    private AiCopilotResponse buildHighRiskIncidentSummary(
            Long organizationId
    ) {
        IncidentInsightDTO insight =
                incidentInsightService.getInsight(organizationId);

        List<AiCopilotDetailDTO> details =
                incidentDetailService.getHighRiskIncidents(organizationId);

        String answer;

        if (details.isEmpty()) {
            answer = """
                No active high-risk or critical incidents were found.

                Current active high-risk incident count: %d
                """.formatted(
                    insight.highRiskIncidents()
            );
        } else {
            answer = """
                High-risk incident review:

                • Active high-risk incidents: %d
                • Showing the most recent %d HIGH or CRITICAL active incidents.

                Prioritize supervisor review and required follow-up.
                """.formatted(
                    insight.highRiskIncidents(),
                    details.size()
            );
        }

        return new AiCopilotResponse(
                answer,
                List.of(
                        new AiCopilotCardDTO(
                                "High Risk",
                                String.valueOf(insight.highRiskIncidents()),
                                "Active priority incidents",
                                insight.highRiskIncidents() > 0
                                        ? "red"
                                        : "green"
                        ),
                        new AiCopilotCardDTO(
                                "Under Review",
                                String.valueOf(insight.underReviewIncidents()),
                                "Active case review",
                                "blue"
                        )
                ),
                List.of(
                        new AiCopilotActionDTO(
                                "Open Incidents",
                                "/incidents"
                        )
                ),
                details,
                "INCIDENT",
                insight.highRiskIncidents() > 0
                        ? "HIGH"
                        : "NORMAL"
        );
    }
    private AiCopilotResponse buildStateReportableIncidentSummary(
            Long organizationId
    ) {
        IncidentInsightDTO insight =
                incidentInsightService.getInsight(organizationId);

        List<AiCopilotDetailDTO> details =
                incidentDetailService.getStateReportableIncidents(
                        organizationId
                );

        String answer;

        if (details.isEmpty()) {
            answer = """
                No active state-reportable incidents were found.

                Current active state-reportable count: %d
                """.formatted(
                    insight.stateReportableIncidents()
            );
        } else {
            answer = """
                State-reportable incident review:

                • Active state-reportable incidents: %d
                • Showing the most recent %d active reportable incidents.

                Review reporting requirements and supervisor follow-up immediately.
                """.formatted(
                    insight.stateReportableIncidents(),
                    details.size()
            );
        }

        return new AiCopilotResponse(
                answer,
                List.of(
                        new AiCopilotCardDTO(
                                "State Reportable",
                                String.valueOf(
                                        insight.stateReportableIncidents()
                                ),
                                "Active regulatory attention",
                                insight.stateReportableIncidents() > 0
                                        ? "red"
                                        : "green"
                        ),
                        new AiCopilotCardDTO(
                                "High Risk",
                                String.valueOf(insight.highRiskIncidents()),
                                "Active priority incidents",
                                insight.highRiskIncidents() > 0
                                        ? "orange"
                                        : "green"
                        )
                ),
                List.of(
                        new AiCopilotActionDTO(
                                "Open Incidents",
                                "/incidents"
                        )
                ),
                details,
                "INCIDENT",
                insight.stateReportableIncidents() > 0
                        ? "CRITICAL"
                        : "NORMAL"
        );
    }
    private AiCopilotResponse buildClientLookup(
            Long organizationId,
            String message
    ) {
        String searchTerm =
                clientNameExtractor.extract(message);

        if (searchTerm.isBlank()) {
            return new AiCopilotResponse(
                    "Please include the client's name so I can search the agency's client records.",
                    List.of(),
                    List.of(
                            new AiCopilotActionDTO(
                                    "Open Clients",
                                    "/clients"
                            )
                    )
            );
        }

        List<AiCopilotDetailDTO> details =
                clientDetailService.searchClients(
                        organizationId,
                        searchTerm
                );

        if (details.isEmpty()) {
            return new AiCopilotResponse(
                    "I could not find a client matching \"" + searchTerm + "\" in your organization.",
                    List.of(),
                    List.of(
                            new AiCopilotActionDTO(
                                    "Open Clients",
                                    "/clients"
                            )
                    ),
                    List.of(),
                    "CLIENT",
                    "NORMAL"
            );
        }

        String answer;

        if (details.size() == 1) {
            answer = """
                I found one matching client.

                Review the client's current operational profile below.
                """;
        } else {
            answer = """
                I found %d clients matching "%s".

                Review the matching records below and select the correct client.
                """.formatted(
                    details.size(),
                    searchTerm
            );
        }

        return new AiCopilotResponse(
                answer,
                List.of(
                        new AiCopilotCardDTO(
                                "Matches",
                                String.valueOf(details.size()),
                                "Matching client records",
                                "blue"
                        )
                ),
                List.of(
                        new AiCopilotActionDTO(
                                "Open Clients",
                                "/clients"
                        )
                ),
                details,
                "CLIENT",
                determineDetailSeverity(details)
        );
    }
    private AiCopilotResponse buildActiveClientSummary(
            Long organizationId
    ) {
        List<AiCopilotDetailDTO> details =
                clientDetailService.getActiveClients(
                        organizationId
                );

        long activeClientCount =
                clientDetailService.getActiveClientCount(
                        organizationId
                );

        String answer = """
            Active client summary:

            • Active clients: %d
            • Showing up to %d client records.

            Review client operational profiles below.
            """.formatted(
                activeClientCount,
                details.size()
        );

        return new AiCopilotResponse(
                answer,
                List.of(
                        new AiCopilotCardDTO(
                                "Active Clients",
                                String.valueOf(activeClientCount),
                                "Current active clients",
                                "blue"
                        )
                ),
                List.of(
                        new AiCopilotActionDTO(
                                "Open Clients",
                                "/clients"
                        )
                ),
                details,
                "CLIENT",
                determineDetailSeverity(details)
        );
    }
    private AiCopilotResponse buildClientsWithIncidents(
            Long organizationId
    ) {
        List<AiCopilotDetailDTO> details =
                clientDetailService
                        .getClientsWithActiveIncidents(
                                organizationId
                        );

        String answer = details.isEmpty()
                ? "No active clients currently have active incidents."
                : """
              Client incident risk review:

              • %d clients with active incidents are shown below.

              Review clients with high-risk active incidents first.
              """.formatted(details.size());

        return new AiCopilotResponse(
                answer,
                List.of(
                        new AiCopilotCardDTO(
                                "Clients With Incidents",
                                String.valueOf(details.size()),
                                "Active incident risk",
                                details.isEmpty()
                                        ? "green"
                                        : "red"
                        )
                ),
                List.of(
                        new AiCopilotActionDTO(
                                "Open Incidents",
                                "/incidents"
                        ),
                        new AiCopilotActionDTO(
                                "Open Clients",
                                "/clients"
                        )
                ),
                details,
                "CLIENT",
                determineDetailSeverity(details)
        );
    }
    private String determineDetailSeverity(
            List<AiCopilotDetailDTO> details
    ) {
        if (details == null || details.isEmpty()) {
            return "NORMAL";
        }

        boolean critical = details.stream()
                .anyMatch(detail ->
                        "CRITICAL".equalsIgnoreCase(
                                detail.getSeverity()
                        )
                );

        if (critical) {
            return "CRITICAL";
        }

        boolean high = details.stream()
                .anyMatch(detail ->
                        "HIGH".equalsIgnoreCase(
                                detail.getSeverity()
                        )
                );

        if (high) {
            return "HIGH";
        }

        boolean medium = details.stream()
                .anyMatch(detail ->
                        "MEDIUM".equalsIgnoreCase(
                                detail.getSeverity()
                        )
                );

        return medium
                ? "MEDIUM"
                : "NORMAL";
    }
    private AiCopilotResponse buildClientsWithEVVIssues(
            Long organizationId
    ) {
        List<AiCopilotDetailDTO> details =
                clientDetailService
                        .getClientsWithOpenEVVIssues(
                                organizationId
                        );

        String answer = details.isEmpty()
                ? "No active clients currently have open EVV exceptions."
                : """
              Client EVV risk review:

              • %d clients with open EVV issues are shown below.

              Prioritize clients with high-severity EVV exceptions.
              """.formatted(details.size());

        return new AiCopilotResponse(
                answer,
                List.of(
                        new AiCopilotCardDTO(
                                "Clients With EVV Issues",
                                String.valueOf(details.size()),
                                "Open EVV risk",
                                details.isEmpty()
                                        ? "green"
                                        : "red"
                        )
                ),
                List.of(
                        new AiCopilotActionDTO(
                                "Review EVV Exceptions",
                                "/evv-exceptions"
                        ),
                        new AiCopilotActionDTO(
                                "Open Clients",
                                "/clients"
                        )
                ),
                details,
                "CLIENT",
                determineDetailSeverity(details)
        );
    }

    private AiCopilotResponse resolveUnknownIntent(
            Long organizationId,
            String message
    ) {
        String clientSearchTerm =
                clientNameExtractor.extract(message);

        if (!clientSearchTerm.isBlank()) {
            List<AiCopilotDetailDTO> clientMatches =
                    clientDetailService.searchClients(
                            organizationId,
                            clientSearchTerm
                    );

            if (!clientMatches.isEmpty()) {
                return buildClientLookup(
                        organizationId,
                        message
                );
            }
        }

        String caregiverSearchTerm =
                caregiverNameExtractor.extract(message);

        if (!caregiverSearchTerm.isBlank()) {
            List<AiCopilotDetailDTO> caregiverMatches =
                    caregiverDetailService.searchCaregivers(
                            organizationId,
                            caregiverSearchTerm
                    );

            if (!caregiverMatches.isEmpty()) {
                return buildCaregiverLookup(
                        organizationId,
                        message
                );
            }
        }

        return defaultHelpResponse();
    }
    private AiCopilotResponse buildCaregiverLookup(
            Long organizationId,
            String message
    ) {
        String searchTerm =
                caregiverNameExtractor.extract(message);

        if (searchTerm.isBlank()) {
            return new AiCopilotResponse(
                    "Please include the caregiver's name so I can search the agency's caregiver records.",
                    List.of(),
                    List.of(
                            new AiCopilotActionDTO(
                                    "Open Caregivers",
                                    "/caregivers"
                            )
                    )
            );
        }

        List<AiCopilotDetailDTO> details =
                caregiverDetailService.searchCaregivers(
                        organizationId,
                        searchTerm
                );

        if (details.isEmpty()) {
            return new AiCopilotResponse(
                    "I could not find a caregiver matching \"" +
                            searchTerm +
                            "\" in your organization.",
                    List.of(),
                    List.of(
                            new AiCopilotActionDTO(
                                    "Open Caregivers",
                                    "/caregivers"
                            )
                    ),
                    List.of(),
                    "CAREGIVER",
                    "NORMAL"
            );
        }

        String answer;

        if (details.size() == 1) {
            answer = """
                I found one matching caregiver.

                Review the caregiver's current operational profile below.
                """;
        } else {
            answer = """
                I found %d caregivers matching "%s".

                Review the matching records below and select the correct caregiver.
                """.formatted(
                    details.size(),
                    searchTerm
            );
        }

        return new AiCopilotResponse(
                answer,
                List.of(
                        new AiCopilotCardDTO(
                                "Matches",
                                String.valueOf(details.size()),
                                "Matching caregiver records",
                                "blue"
                        )
                ),
                List.of(
                        new AiCopilotActionDTO(
                                "Open Caregivers",
                                "/caregivers"
                        )
                ),
                details,
                "CAREGIVER",
                determineDetailSeverity(details)
        );
    }
    private AiCopilotResponse buildActiveCaregiverSummary(
            Long organizationId
    ) {
        List<AiCopilotDetailDTO> details =
                caregiverDetailService.getActiveCaregivers(
                        organizationId
                );

        long activeCaregiverCount =
                caregiverDetailService.getActiveCaregiverCount(
                        organizationId
                );

        String answer = """
            Active caregiver summary:

            • Active caregivers: %d
            • Showing up to %d caregiver records.

            Review caregiver operational profiles below.
            """.formatted(
                activeCaregiverCount,
                details.size()
        );

        return new AiCopilotResponse(
                answer,
                List.of(
                        new AiCopilotCardDTO(
                                "Active Caregivers",
                                String.valueOf(activeCaregiverCount),
                                "Current active caregivers",
                                "blue"
                        )
                ),
                List.of(
                        new AiCopilotActionDTO(
                                "Open Caregivers",
                                "/caregivers"
                        )
                ),
                details,
                "CAREGIVER",
                determineDetailSeverity(details)
        );
    }
    private AiCopilotResponse buildCaregiversWithEVVIssues(
            Long organizationId
    ) {
        List<AiCopilotDetailDTO> details =
                caregiverDetailService
                        .getCaregiversWithOpenEVVIssues(
                                organizationId
                        );

        String answer = details.isEmpty()
                ? "No active caregivers currently have open EVV exceptions."
                : """
              Caregiver EVV review:

              • %d caregivers with open EVV issues are shown below.

              Review caregivers with high-severity EVV exceptions first.
              """.formatted(details.size());

        return new AiCopilotResponse(
                answer,
                List.of(
                        new AiCopilotCardDTO(
                                "Caregivers With EVV Issues",
                                String.valueOf(details.size()),
                                "Open EVV risk",
                                details.isEmpty()
                                        ? "green"
                                        : "red"
                        )
                ),
                List.of(
                        new AiCopilotActionDTO(
                                "Review EVV Exceptions",
                                "/evv-exceptions"
                        ),
                        new AiCopilotActionDTO(
                                "Open Caregivers",
                                "/caregivers"
                        )
                ),
                details,
                "CAREGIVER",
                determineDetailSeverity(details)
        );
    }
    private AiCopilotResponse buildCaregiversWithHighSeverityEVV(
            Long organizationId
    ) {
        List<AiCopilotDetailDTO> details =
                caregiverDetailService
                        .getCaregiversWithHighSeverityEVV(
                                organizationId
                        );

        String answer = details.isEmpty()
                ? "No active caregivers currently have open high-severity EVV exceptions."
                : """
              High-severity caregiver EVV review:

              • %d caregivers with open high-severity EVV exceptions are shown below.

              Prioritize these caregiver records for supervisor review.
              """.formatted(details.size());

        return new AiCopilotResponse(
                answer,
                List.of(
                        new AiCopilotCardDTO(
                                "High-Risk Caregivers",
                                String.valueOf(details.size()),
                                "High-severity EVV risk",
                                details.isEmpty()
                                        ? "green"
                                        : "red"
                        )
                ),
                List.of(
                        new AiCopilotActionDTO(
                                "Review EVV Exceptions",
                                "/evv-exceptions"
                        ),
                        new AiCopilotActionDTO(
                                "Open Caregivers",
                                "/caregivers"
                        )
                ),
                details,
                "CAREGIVER",
                determineDetailSeverity(details)
        );
    }
}