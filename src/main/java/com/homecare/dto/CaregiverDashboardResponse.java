package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CaregiverDashboardResponse {

    private Long caregiverId;

    private String caregiverName;

    private Long todayAppointments;

    private Long completedAppointments;

    private Long pendingAppointments;

    private Long totalVisitNotes;

    private Long totalClockRecords;

    private Long medicationLogs;
}