package com.homecare.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "assignments")
public class AssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate assignmentDate;

    private String status;

    @ManyToOne
    @JoinColumn(name = "caregiver_id")
    private CaregiverEntity caregiver;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private ClientEntity client;

    public Long getId() {
        return id;
    }

    public LocalDate getAssignmentDate() {
        return assignmentDate;
    }

    public String getStatus() {
        return status;
    }

    public CaregiverEntity getCaregiver() {
        return caregiver;
    }

    public ClientEntity getClient() {
        return client;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAssignmentDate(LocalDate assignmentDate) {
        this.assignmentDate = assignmentDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCaregiver(CaregiverEntity caregiver) {
        this.caregiver = caregiver;
    }

    public void setClient(ClientEntity client) {
        this.client = client;
    }
}