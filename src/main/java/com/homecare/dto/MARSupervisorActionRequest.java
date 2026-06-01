package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MARSupervisorActionRequest {

    private Long medicationLogId;
    private Long supervisorId;

    private String actionStatus;
    private String supervisorNotes;
    private Long actorUserId;
}