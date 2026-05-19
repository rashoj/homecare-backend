package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MissedMedicationAlertResponse {

    private Long logId;

    private Long clientId;
    private String clientName;

    private Long medicationId;
    private String medicationName;

    private Long caregiverId;
    private String caregiverName;

    private LocalDateTime scheduledAt;
    private LocalDateTime givenAt;

    private String status;
    private String missedReason;
    private String notes;
}