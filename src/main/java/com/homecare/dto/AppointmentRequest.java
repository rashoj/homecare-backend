package com.homecare.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NotNull
@NotBlank
@Size
@FutureOrPresent
public class AppointmentRequest {

    private Long clientId;

    private Long caregiverId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String serviceType;

    private String shiftType;

    private String status;

    private Boolean evvRequired;

    private Boolean billable;

    private String repeatType;
    // NONE, DAILY, WEEKLY

    private LocalDate repeatUntil;

    private String notes;

    private Long createdByUserId;

}