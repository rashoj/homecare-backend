package com.homecare.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MARAlertResponse {

    private String alertType; // MISSED, REFUSED, HELD, PRN_GIVEN, OVERDUE

    private Long medicationId;
    private String medicationName;

    private Long clientId;
    private String clientName;

    private Long caregiverId;
    private String caregiverName;

    private LocalDateTime scheduledAt;
    private LocalDateTime givenAt;

    private String status;
    private String reason;
    private String notes;
}