package com.homecare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ClientRequest {

    @NotBlank(message = "Client name is required")
    private String fullName;

    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    private String address;

    private String emergencyContactName;

    private String emergencyContactPhone;

    private String allergies;

    private String medicalConditions;

    private String carePlan;

    private String mobilityStatus;

    private Double latitude;

    private Double longitude;
}