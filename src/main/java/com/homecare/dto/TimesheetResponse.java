package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TimesheetResponse {

    private Long id;

    private Long clockRecordId;
    private Long appointmentId;

    private Long clientId;
    private String clientName;

    private Long caregiverId;
    private String caregiverName;

    private Long authorizationId;
    private String authorizationNumber;

    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;

    private Double totalHours;
    private Double regularHours;
    private Double overtimeHours;

    private Double caregiverPayRate;
    private Double caregiverPayAmount;

    private Double billingRate;
    private Double billableAmount;

    private String payrollStatus;
    private String billingStatus;

    private Boolean documentationApproved;
    private Boolean authorizationValid;
    private Boolean billable;

    private String notes;

    private LocalDateTime reviewedAt;

    private Boolean authorizationOverride;
    private String authorizationOverrideReason;
    private LocalDateTime authorizationOverrideAt;
}