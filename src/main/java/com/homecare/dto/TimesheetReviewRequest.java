package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimesheetReviewRequest {

    private Double caregiverPayRate;

    private Double billingRate;

    private String payrollStatus;

    private String billingStatus;

    private Boolean billable;

    private String notes;

    private Boolean authorizationOverride;

    private String authorizationOverrideReason;
}