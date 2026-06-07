package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAgencyAdminRequest {
    private String fullName;
    private String email;
    private String password;
    private String phoneNumber;
}