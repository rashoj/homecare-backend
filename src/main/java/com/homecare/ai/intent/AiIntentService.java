package com.homecare.ai.intent;

import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AiIntentService {

    private static final double CONFIDENCE_VERY_HIGH = 0.99;
    private static final double CONFIDENCE_HIGH = 0.98;
    private static final double CONFIDENCE_STANDARD = 0.96;
    private static final double CONFIDENCE_LOOKUP = 0.95;
    private static final double CONFIDENCE_UNKNOWN = 0.20;

    public AiIntentResult detect(String message) {

        if (message == null || message.isBlank()) {
            return result(
                    AiIntent.UNKNOWN,
                    "",
                    "",
                    0.0
            );
        }

        String normalized = normalize(message);

        /*
         * Specific intents must always be evaluated
         * before broad domain-level intents.
         */

        if (
                containsAny(
                        normalized,
                        "client",
                        "clients",
                        "patient",
                        "patients"
                )
                        && containsAny(
                        normalized,
                        "evv issue",
                        "evv issues",
                        "evv exception",
                        "evv exceptions"
                )
        ) {
            return result(
                    AiIntent.CLIENT_WITH_EVV_ISSUES,
                    message,
                    normalized,
                    CONFIDENCE_VERY_HIGH
            );
        }

        if (
                containsAny(
                        normalized,
                        "client",
                        "clients",
                        "patient",
                        "patients"
                )
                        && containsAny(
                        normalized,
                        "incident",
                        "incidents",
                        "active incident",
                        "active incidents"
                )
        ) {
            return result(
                    AiIntent.CLIENT_WITH_INCIDENTS,
                    message,
                    normalized,
                    CONFIDENCE_VERY_HIGH
            );
        }

        if (
                containsAny(
                        normalized,
                        "active client",
                        "active clients",
                        "current client",
                        "current clients"
                )
        ) {
            return result(
                    AiIntent.CLIENT_ACTIVE,
                    message,
                    normalized,
                    CONFIDENCE_VERY_HIGH
            );
        }

        if (
                containsAny(
                        normalized,
                        "evv",
                        "exception",
                        "clock"
                )
                        && containsAny(
                        normalized,
                        "high severity",
                        "high-severity",
                        "critical",
                        "highest severity",
                        "severe"
                )
        ) {
            return result(
                    AiIntent.EVV_HIGH_SEVERITY,
                    message,
                    normalized,
                    CONFIDENCE_VERY_HIGH
            );
        }

        if (
                containsAny(
                        normalized,
                        "shift",
                        "shifts",
                        "staffing",
                        "coverage"
                )
                        && containsAny(
                        normalized,
                        "urgent",
                        "high priority",
                        "high-priority",
                        "priority",
                        "highest priority"
                )
        ) {
            return result(
                    AiIntent.OPEN_SHIFT_PRIORITY,
                    message,
                    normalized,
                    CONFIDENCE_VERY_HIGH
            );
        }

        if (
                containsAny(
                        normalized,
                        "incident",
                        "incidents"
                )
                        && containsAny(
                        normalized,
                        "state reportable",
                        "state-reportable",
                        "reportable",
                        "regulatory"
                )
        ) {
            return result(
                    AiIntent.INCIDENT_STATE_REPORTABLE,
                    message,
                    normalized,
                    CONFIDENCE_VERY_HIGH
            );
        }

        if (
                containsAny(
                        normalized,
                        "incident",
                        "incidents"
                )
                        && containsAny(
                        normalized,
                        "high risk",
                        "high-risk",
                        "critical",
                        "highest risk",
                        "severe"
                )
        ) {
            return result(
                    AiIntent.INCIDENT_HIGH_RISK,
                    message,
                    normalized,
                    CONFIDENCE_VERY_HIGH
            );
        }

        if (
                containsAny(
                        normalized,
                        "today",
                        "today's",
                        "summary",
                        "operations",
                        "operational summary",
                        "dashboard",
                        "agency status",
                        "agency health"
                )
        ) {
            return result(
                    AiIntent.TODAY_SUMMARY,
                    message,
                    normalized,
                    CONFIDENCE_HIGH
            );
        }

        if (
                containsAny(
                        normalized,
                        "evv",
                        "clock",
                        "clock in",
                        "clock-in",
                        "clock out",
                        "clock-out",
                        "exception",
                        "exceptions",
                        "evv alert",
                        "evv alerts"
                )
        ) {
            return result(
                    AiIntent.EVV_REVIEW,
                    message,
                    normalized,
                    0.97
            );
        }

        if (
                containsAny(
                        normalized,
                        "open shift",
                        "open shifts",
                        "shift",
                        "shifts",
                        "staffing",
                        "staffing coverage",
                        "coverage"
                )
        ) {
            return result(
                    AiIntent.OPEN_SHIFTS,
                    message,
                    normalized,
                    CONFIDENCE_STANDARD
            );
        }

        if (
                containsAny(
                        normalized,
                        "incident",
                        "incidents",
                        "risk",
                        "reportable"
                )
        ) {
            return result(
                    AiIntent.INCIDENT_REVIEW,
                    message,
                    normalized,
                    CONFIDENCE_STANDARD
            );
        }

        if (
                containsAny(
                        normalized,
                        "client",
                        "clients",
                        "patient",
                        "patients"
                )
        ) {
            return result(
                    AiIntent.CLIENT_LOOKUP,
                    message,
                    normalized,
                    CONFIDENCE_LOOKUP
            );
        }

        if (
                containsAny(
                        normalized,
                        "caregiver",
                        "caregivers",
                        "employee",
                        "employees",
                        "worker",
                        "workers"
                )
                        && containsAny(
                        normalized,
                        "high severity evv",
                        "high-severity evv",
                        "high severity exception",
                        "high-severity exception",
                        "high severity exceptions",
                        "high-severity exceptions"
                )
        ) {
            return result(
                    AiIntent.CAREGIVER_WITH_HIGH_SEVERITY_EVV,
                    message,
                    normalized,
                    CONFIDENCE_VERY_HIGH
            );
        }

        if (
                containsAny(
                        normalized,
                        "caregiver",
                        "caregivers",
                        "employee",
                        "employees",
                        "worker",
                        "workers"
                )
                        && containsAny(
                        normalized,
                        "evv issue",
                        "evv issues",
                        "evv exception",
                        "evv exceptions"
                )
        ) {
            return result(
                    AiIntent.CAREGIVER_WITH_EVV_ISSUES,
                    message,
                    normalized,
                    CONFIDENCE_VERY_HIGH
            );
        }

        if (
                containsAny(
                        normalized,
                        "active caregiver",
                        "active caregivers",
                        "current caregiver",
                        "current caregivers"
                )
        ) {
            return result(
                    AiIntent.CAREGIVER_ACTIVE,
                    message,
                    normalized,
                    CONFIDENCE_VERY_HIGH
            );
        }

        if (
                containsAny(
                        normalized,
                        "caregiver",
                        "caregivers",
                        "employee",
                        "employees",
                        "worker",
                        "workers",
                        "staff member"
                )
        ) {
            return result(
                    AiIntent.CAREGIVER_LOOKUP,
                    message,
                    normalized,
                    CONFIDENCE_LOOKUP
            );
        }

        return result(
                AiIntent.UNKNOWN,
                message,
                normalized,
                CONFIDENCE_UNKNOWN
        );
    }

    private String normalize(String message) {
        return message
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }

    private boolean containsAny(
            String message,
            String... keywords
    ) {
        if (
                message == null
                        || keywords == null
        ) {
            return false;
        }

        for (String keyword : keywords) {
            if (
                    keyword != null
                            && !keyword.isBlank()
                            && message.contains(keyword)
            ) {
                return true;
            }
        }

        return false;
    }

    private AiIntentResult result(
            AiIntent intent,
            String originalMessage,
            String normalizedMessage,
            double confidence
    ) {
        return new AiIntentResult(
                intent,
                originalMessage,
                normalizedMessage,
                confidence
        );
    }
}