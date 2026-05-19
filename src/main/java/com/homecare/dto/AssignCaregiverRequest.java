package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AssignCaregiverRequest {

    private Long clientId;

    private Long caregiverId;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean primaryCaregiver;
}