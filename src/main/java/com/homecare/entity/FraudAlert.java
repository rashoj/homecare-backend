package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "caregiver_id")
    private User caregiver;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    private Long visitId;

    private String alertType;
    private String severity;
    private Integer riskScore;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String status;

    private LocalDateTime detectedAt;
    private LocalDateTime resolvedAt;

    private Long resolvedBy;

    @PrePersist
    public void onCreate() {
        if (detectedAt == null) detectedAt = LocalDateTime.now();
        if (status == null) status = "OPEN";
        if (riskScore == null) riskScore = 0;
    }
}