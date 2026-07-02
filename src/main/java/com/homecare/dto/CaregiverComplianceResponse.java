package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CaregiverComplianceResponse {
    private Long id;

    private Long caregiverId;
    private String caregiverName;

    private Long organizationId;

    private String recordType;
    private String status;

    private LocalDate completedDate;
    private LocalDate expirationDate;

    private String notes;

    private Long verifiedBy;
    private LocalDateTime verifiedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}