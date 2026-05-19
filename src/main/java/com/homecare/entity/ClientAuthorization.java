package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "client_authorizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientAuthorization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    private String authorizationNumber;

    private String serviceCode;

    private LocalDate startDate;

    private LocalDate endDate;

    private Double approvedWeeklyHours;

    private Double approvedTotalHours;

    private Double usedHours;

    private String status;
    // ACTIVE, EXPIRED, CLOSED

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.usedHours == null) {
            this.usedHours = 0.0;
        }

        if (this.status == null) {
            this.status = "ACTIVE";
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}