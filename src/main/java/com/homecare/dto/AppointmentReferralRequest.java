package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AppointmentReferralRequest {

    private Long clientId;
    private Long caregiverId;

    private String clientFullName;
    private String clientPhone;
    private String clientEmail;
    private String clientAddress;

    private String referralSource;
    private String hospitalName;
    private String dischargePlannerName;
    private String dischargePlannerPhone;

    private LocalDateTime requestedStartTime;
    private LocalDateTime requestedEndTime;

    private String serviceType;
    private String notes;
}