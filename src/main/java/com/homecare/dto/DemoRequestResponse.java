package com.homecare.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemoRequestResponse {
    private Long id;
    private String fullName;
    private String agencyName;
    private String email;
    private String phone;
    private String agencySize;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}