package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "isp_goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ISPGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private ISPPlan ispPlan;

    @ManyToOne
    private Client client;

    private String goalTitle;

    @Column(columnDefinition = "TEXT")
    private String goalDescription;

    private String category; // ADL, BEHAVIOR, COMMUNITY, COMMUNICATION, SAFETY

    private LocalDate targetDate;

    private String status; // ACTIVE, COMPLETED, DISCONTINUED

    private LocalDateTime createdAt;
}