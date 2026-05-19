package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ClientCaregiverResponse {

    private Long id;

    private Long clientId;

    private String clientName;

    private Long caregiverId;

    private String caregiverName;

    private String caregiverEmail;

    private String caregiverRole;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean active;

    private Boolean primaryCaregiver;
}