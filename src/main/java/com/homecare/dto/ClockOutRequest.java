package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClockOutRequest {

    private Long appointmentId;

    private Double latitude;

    private Double longitude;

    private String notes;
}