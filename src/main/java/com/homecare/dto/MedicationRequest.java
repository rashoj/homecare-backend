package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class MedicationRequest {

    private Long clientId;
    private String medicationName;
    private String dosage;
    private String frequency;
    private LocalTime scheduledTime;
    private String instructions;
}