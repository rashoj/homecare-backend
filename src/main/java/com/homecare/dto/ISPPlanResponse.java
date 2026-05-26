package com.homecare.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ISPPlanResponse {

    private Long id;
    private Long clientId;
    private String clientName;

    private String planName;
    private LocalDate startDate;
    private LocalDate endDate;

    private String status;
    private String notes;

    private LocalDateTime createdAt;
}