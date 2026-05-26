package com.homecare.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BehaviorOptionResponse {
    private String value;
    private String label;
}