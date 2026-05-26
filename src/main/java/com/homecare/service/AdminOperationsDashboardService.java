package com.homecare.service;

import com.homecare.dto.AdminOperationsDashboardResponse;
import com.homecare.repository.ClockRecordRepository;
import com.homecare.repository.EVVAlertRepository;
import com.homecare.repository.EVVExceptionRepository;
import com.homecare.repository.ServiceDocumentationRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminOperationsDashboardService {

    private final EVVAlertRepository evvAlertRepository;
    private final EVVExceptionRepository evvExceptionRepository;
    private final ClockRecordRepository clockRecordRepository;
    private final ServiceDocumentationRepository serviceDocumentationRepository;

    public AdminOperationsDashboardService(
            EVVAlertRepository evvAlertRepository,
            EVVExceptionRepository evvExceptionRepository,
            ClockRecordRepository clockRecordRepository,
            ServiceDocumentationRepository serviceDocumentationRepository
    ) {
        this.evvAlertRepository = evvAlertRepository;
        this.evvExceptionRepository = evvExceptionRepository;
        this.clockRecordRepository = clockRecordRepository;
        this.serviceDocumentationRepository = serviceDocumentationRepository;
    }

    public AdminOperationsDashboardResponse getSummary() {
        long unreadEVVAlerts =
                evvAlertRepository.countByStatus("UNREAD");

        long openEVVExceptions =
                evvExceptionRepository.countByStatus("OPEN");

        long highSeverityEVVExceptions =
                evvExceptionRepository.countBySeverity("HIGH");

        long caregiversClockedIn =
                clockRecordRepository.countByStatus("CLOCKED_IN");

        long pendingServiceDocumentation =
                serviceDocumentationRepository.countByStatus("SUBMITTED");

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
    }}