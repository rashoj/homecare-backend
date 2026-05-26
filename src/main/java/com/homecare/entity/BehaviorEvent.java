package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "behavior_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BehaviorEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Client client;

    @ManyToOne
    private User caregiver;

    @ManyToOne
    private Appointment appointment;

    @ManyToOne
    private ServiceDocumentation serviceDocumentation;

    private String behaviorType;
    // AGGRESSION, SELF_INJURY, ELOPEMENT, REFUSAL, VERBAL_OUTBURST, PROPERTY_DESTRUCTION

    private String trigger;
    // DEMAND_PLACED, TRANSITION, DENIED_ACCESS, LOUD_NOISE, UNKNOWN

    private String severity;
    // LOW, MEDIUM, HIGH, CRITICAL

    private Integer durationMinutes;

    @Column(columnDefinition = "TEXT")
    private String interventionUsed;

    private String outcome;
    // RESOLVED, ESCALATED, SUPERVISOR_NOTIFIED, INCIDENT_REPORT_REQUIRED

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime createdAt;
}