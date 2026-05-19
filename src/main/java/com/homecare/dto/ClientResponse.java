package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ClientResponse {

    private Long id;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phoneNumber;
    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String allergies;
    private String medicalConditions;
    private String carePlan;
    private String mobilityStatus;
    private Boolean active;
    private Double latitude;
    private Double longitude;
}