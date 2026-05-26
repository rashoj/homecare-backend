package com.homecare.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NotNull
@NotBlank
@Size
@FutureOrPresent

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