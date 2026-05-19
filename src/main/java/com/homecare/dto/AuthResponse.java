package com.homecare.dto;

import com.homecare.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AuthResponse {

    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private String token;
    private String message;
}