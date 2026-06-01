package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentReferralConvertRequest {
    private Long clientId;
    private Long caregiverId;
    private Long convertedByUserId;

    private String serviceType;
    private String shiftType;
    private Boolean evvRequired;
    private Boolean billable;
    private String notes;
}