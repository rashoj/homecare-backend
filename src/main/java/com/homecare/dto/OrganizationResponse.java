package com.homecare.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationResponse {
    private Long id;

    private String name;
    private String legalName;
    private String email;
    private String phone;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;

    private String medicaidProviderNumber;
    private String npiNumber;

    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}