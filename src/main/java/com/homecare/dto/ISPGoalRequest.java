package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ISPGoalRequest {

    private Long ispPlanId;
    private Long clientId;

    private String goalTitle;
    private String goalDescription;
    private String category;

    private LocalDate targetDate;
}