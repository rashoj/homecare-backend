package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "evv_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EVVAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long exceptionId;
    private Long clientId;
    private Long caregiverId;
    private Long appointmentId;

    private String alertType;
    private String severity;
    private String status; // UNREAD, READ, RESOLVED

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}