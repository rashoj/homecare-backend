package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String participantRole;
    // FAMILY_MEMBER, ADMIN, CAREGIVER

    private Boolean active;

    private LocalDateTime joinedAt;

    @PrePersist
    public void onCreate() {
        this.joinedAt = LocalDateTime.now();

        if (this.active == null) {
            this.active = true;
        }
    }
}