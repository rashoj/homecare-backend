package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DemoRequestCreateRequest {
    private String fullName;
    private String agencyName;
    private String email;
    private String phone;
    private String agencySize;
    private String message;
}