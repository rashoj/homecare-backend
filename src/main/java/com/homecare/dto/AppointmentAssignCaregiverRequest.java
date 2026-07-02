// src/main/java/com/homecare/dto/AppointmentAssignCaregiverRequest.java
package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentAssignCaregiverRequest {
    private Long caregiverId;
    private String notes;
}