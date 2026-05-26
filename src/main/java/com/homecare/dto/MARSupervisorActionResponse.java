package com.homecare.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MARSupervisorActionResponse {

    private Long id;

    private Long medicationLogId;

    private Long supervisorId;
    private String supervisorName;

    private String actionStatus;
    private String supervisorNotes;

    private LocalDateTime createdAt;
}