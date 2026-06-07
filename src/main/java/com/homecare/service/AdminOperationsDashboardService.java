package com.homecare.service;

import com.homecare.dto.AdminOperationsDashboardResponse;
import com.homecare.entity.Organization;
import com.homecare.entity.User;
import com.homecare.repository.*;
import org.springframework.stereotype.Service;

@Service
public class AdminOperationsDashboardService {

    private final EVVAlertRepository evvAlertRepository;
    private final EVVExceptionRepository evvExceptionRepository;
    private final ClockRecordRepository clockRecordRepository;
    private final ServiceDocumentationRepository serviceDocumentationRepository;
    private final UserRepository userRepository;

    public AdminOperationsDashboardService(
            EVVAlertRepository evvAlertRepository,
            EVVExceptionRepository evvExceptionRepository,
            ClockRecordRepository clockRecordRepository,
            ServiceDocumentationRepository serviceDocumentationRepository,UserRepository userRepository
    ) {
        this.evvAlertRepository = evvAlertRepository;
        this.evvExceptionRepository = evvExceptionRepository;
        this.clockRecordRepository = clockRecordRepository;
        this.serviceDocumentationRepository = serviceDocumentationRepository;
        this.userRepository = userRepository;
    }

    public AdminOperationsDashboardResponse getSummary(String actorEmail) {
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Organization organization = actor.getOrganization();

        if (organization == null || organization.getId() == null) {
            throw new RuntimeException("User is not assigned to an organization");
        }

        Long organizationId = organization.getId();

        long unreadEVVAlerts =
                evvAlertRepository.countByOrganizationIdAndStatus(
                        organizationId,
                        "UNREAD"
                );
        long openEVVExceptions =
                evvExceptionRepository.countByOrganizationIdAndStatus(
                        organizationId,
                        "OPEN"
                );

        long highSeverityEVVExceptions =
                evvExceptionRepository.countByOrganizationIdAndSeverity(
                        organizationId,
                        "HIGH"
                );

        long caregiversClockedIn =
                clockRecordRepository.countByAppointmentOrganizationIdAndStatus(
                        organizationId,
                        "CLOCKED_IN"
                );

        long pendingServiceDocumentation =
                serviceDocumentationRepository.countByOrganizationIdAndStatus(
                        organizationId,
                        "SUBMITTED"
                );
        long payrollBlockedItems =
                pendingServiceDocumentation + openEVVExceptions;

        return AdminOperationsDashboardResponse.builder()
                .unreadEVVAlerts(unreadEVVAlerts)
                .openEVVExceptions(openEVVExceptions)
                .highSeverityEVVExceptions(highSeverityEVVExceptions)
                .caregiversClockedIn(caregiversClockedIn)
                .pendingServiceDocumentation(pendingServiceDocumentation)
                .payrollBlockedItems(payrollBlockedItems)
                .build();
    }

}