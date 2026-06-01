package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BillingRecordRequest {

    private Long timesheetId;

    private Long actorUserId;

    private Double billingRate;

    private String status;

    private String claimNumber;

    private Double paidAmount;

    private LocalDate paidDate;

    private String notes;
}