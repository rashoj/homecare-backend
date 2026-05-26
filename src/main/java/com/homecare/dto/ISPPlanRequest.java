package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ISPPlanRequest {

    private Long clientId;
    private String planName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
}