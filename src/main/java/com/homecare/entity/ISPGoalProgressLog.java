package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "isp_goal_progress_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ISPGoalProgressLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private ISPGoal goal;

    @ManyToOne
    private Client client;

    @ManyToOne
    private User caregiver;

    @ManyToOne
    private Appointment appointment;

    @ManyToOne
    private ServiceDocumentation serviceDocumentation;

    private String progressStatus;
    // IMPROVED, MAINTAINED, REGRESSED, NOT_ADDRESSED

    private String promptLevel;
    // INDEPENDENT, VERBAL_PROMPT, PHYSICAL_ASSIST, FULL_ASSIST

    @Column(columnDefinition = "TEXT")
    private String progressNote;

    private LocalDateTime createdAt;
}