package com.homecare.dto;

public class CaregiverAssignmentResponse {

    private Long appointmentId;

    private String status;

    private CaregiverDto caregiver;

    private ClientDto client;

    public CaregiverAssignmentResponse(
            Long appointmentId,
            String status,
            CaregiverDto caregiver,
            ClientDto client
    ) {
        this.appointmentId = appointmentId;
        this.status = status;
        this.caregiver = caregiver;
        this.client = client;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public String getStatus() {
        return status;
    }

    public CaregiverDto getCaregiver() {
        return caregiver;
    }

    public ClientDto getClient() {
        return client;
    }
}