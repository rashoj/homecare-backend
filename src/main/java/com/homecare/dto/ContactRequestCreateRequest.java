package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactRequestCreateRequest {
    private String fullName;
    private String email;
    private String subject;
    private String message;
}