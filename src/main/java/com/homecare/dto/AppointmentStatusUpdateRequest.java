package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentStatusUpdateRequest {

    private String status;

    private String notes;

    private Long updatedByUserId;
}