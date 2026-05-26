package com.homecare.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MARDueMedicationResponse {

    private Long medicationId;
    private Long clientId;
    private String clientName;

    private String medicationName;
    private String dosage;
    private String frequency;
    private LocalTime scheduledTime;
    private LocalDateTime scheduledAt;
    private String instructions;

    private Boolean alreadyLogged;
}