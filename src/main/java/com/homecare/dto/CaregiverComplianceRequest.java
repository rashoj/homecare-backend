package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CaregiverComplianceRequest {
    private Long caregiverId;
    private String recordType;
    private String status;
    private LocalDate completedDate;
    private LocalDate expirationDate;
    private String notes;
}