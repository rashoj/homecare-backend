package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationCreateRequest {
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
}