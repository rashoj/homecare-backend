package com.homecare.dto;

import com.homecare.entity.Role;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NotNull
@NotBlank
@Size
@FutureOrPresent

public class AuthResponse {

    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private String token;
    private String message;
}