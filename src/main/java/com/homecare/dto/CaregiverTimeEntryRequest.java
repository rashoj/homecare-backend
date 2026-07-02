package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CaregiverTimeEntryRequest {

    private String shiftType;

    private Double latitude;
    private Double longitude;

    private String notes;
}