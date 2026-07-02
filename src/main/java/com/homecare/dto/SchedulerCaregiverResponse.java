package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class SchedulerCaregiverResponse {

    private Long caregiverId;
    private String caregiverName;
    private String caregiverEmail;

    private Boolean readyToWork;
    private Boolean schedulingBlocked;
    private Integer readinessScore;

    private Integer todayAppointments;
    private Double scheduledHoursToday;
    private Boolean currentlyClockedIn;

    private List<String> blockingReasons;
    private List<String> warnings;

    private String status;
}