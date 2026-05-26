package com.homecare.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ISPGoalResponse {

    private Long id;

    private Long ispPlanId;
    private Long clientId;
    private String clientName;

    private String goalTitle;
    private String goalDescription;
    private String category;

    private LocalDate targetDate;
    private String status;

    private LocalDateTime createdAt;
}