package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@Builder
public class MedicationResponse {

    private Long id;
    private Long clientId;
    private String clientName;
    private String medicationName;
    private String dosage;
    private String frequency;
    private LocalTime scheduledTime;
    private String instructions;
    private Boolean active;
}