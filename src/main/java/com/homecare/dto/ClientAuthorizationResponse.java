package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ClientAuthorizationResponse {

    private Long id;

    private Long clientId;

    private String clientName;

    private String authorizationNumber;

    private String serviceCode;

    private LocalDate startDate;

    private LocalDate endDate;

    private Double approvedWeeklyHours;

    private Double approvedTotalHours;

    private Double usedHours;

    private Double remainingHours;

    private String status;

    private String alertStatus;
    // OK, EXPIRING_SOON, EXPIRED, OVER_USED

    private String notes;
}