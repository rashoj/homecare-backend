package com.homecare.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOperationsDashboardResponse {

    private long unreadEVVAlerts;
    private long openEVVExceptions;
    private long highSeverityEVVExceptions;
    private long caregiversClockedIn;
    private long pendingServiceDocumentation;
    private long payrollBlockedItems;
}