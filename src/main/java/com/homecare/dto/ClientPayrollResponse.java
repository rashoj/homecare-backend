package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ClientPayrollResponse {

    private Long clientId;

    private String clientName;

    private Double hourlyRate;

    private Double totalHours;

    private Double amountDue;
}