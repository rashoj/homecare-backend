package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DashboardTrendResponse {

    private String label;
    private Long completed;
    private Long missed;
    private Double rate;
    private Long exceptions;
}