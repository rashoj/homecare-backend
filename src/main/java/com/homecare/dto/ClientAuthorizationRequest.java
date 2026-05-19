package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ClientAuthorizationRequest {

    private Long clientId;

    private String authorizationNumber;

    private String serviceCode;

    private LocalDate startDate;

    private LocalDate endDate;

    private Double approvedWeeklyHours;

    private Double approvedTotalHours;

    private String notes;
}