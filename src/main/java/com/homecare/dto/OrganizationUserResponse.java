package com.homecare.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationUserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String phoneNumber;
    private Boolean active;
    private Long organizationId;
    private String organizationName;
    private LocalDateTime createdAt;
}