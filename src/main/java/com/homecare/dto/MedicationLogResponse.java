package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MedicationLogResponse {

    private Long id;

    private Long medicationId;

    private String medicationName;

    private Long clientId;

    private String clientName;

    private Long caregiverId;

    private String caregiverName;

    private LocalDateTime scheduledAt;

    private LocalDateTime givenAt;

    private String status;

    private String notes;

    private Boolean prn;

    private String prnReason;

    private String refusalReason;

    private String missedReason;

    private String caregiverSignature;
}