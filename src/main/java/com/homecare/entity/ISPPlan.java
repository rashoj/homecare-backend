package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "isp_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ISPPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Client client;

    private String planName;

    private LocalDate startDate;

    private LocalDate endDate;

    private String status; // ACTIVE, EXPIRED, ARCHIVED

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime createdAt;
}