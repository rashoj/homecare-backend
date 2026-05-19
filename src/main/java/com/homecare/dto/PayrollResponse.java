package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PayrollResponse {

    private Long caregiverId;

    private String caregiverName;

    private Double hourlyRate;

    private Double totalHours;

    private Double totalPay;
}