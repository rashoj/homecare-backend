package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClockInRequest {

    private Long appointmentId;

    private Double latitude;

    private Double longitude;

    private String notes;

    private Long actorUserId;
}