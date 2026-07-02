package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class FraudAlertResponse {
    private Long id;
    private String alertType;
    private String severity;
    private Integer riskScore;
    private String title;
    private String description;
    private String status;
    private LocalDateTime detectedAt;

    private Long caregiverId;
    private String caregiverName;

    private Long clientId;
    private String clientName;

    private Long visitId;
}