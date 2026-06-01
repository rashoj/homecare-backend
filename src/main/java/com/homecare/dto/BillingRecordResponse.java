package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class BillingRecordResponse {

    private Long id;

    private Long timesheetId;

    private Long clientId;
    private String clientName;

    private Long authorizationId;
    private String authorizationNumber;

    private LocalDate serviceDate;

    private Double units;

    private Double billingRate;

    private Double amount;

    private String status;

    private String claimNumber;

    private Double paidAmount;

    private LocalDate paidDate;

    private String notes;
}