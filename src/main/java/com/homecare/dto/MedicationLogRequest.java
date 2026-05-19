package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedicationLogRequest {

    private Long medicationId;

    private Long caregiverId;

    private String status;

    private String notes;

    private Boolean prn;

    private String prnReason;

    private String refusalReason;

    private String missedReason;

    private String caregiverSignature;

    private String scheduledAt;
}