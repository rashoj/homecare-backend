package com.homecare.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BehaviorEventResponse {

    private Long id;

    private Long clientId;
    private String clientName;

    private Long caregiverId;
    private String caregiverName;

    private Long appointmentId;
    private Long serviceDocumentationId;

    private String behaviorType;
    private String trigger;
    private String severity;
    private Integer durationMinutes;
    private String interventionUsed;
    private String outcome;
    private String notes;

    private LocalDateTime createdAt;
}