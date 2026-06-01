package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "sender_user_id", nullable = false)
    private User sender;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String messageBody;

    private Boolean readByRecipient;

    private LocalDateTime sentAt;

    private LocalDateTime readAt;

    @PrePersist
    public void onCreate() {
        this.sentAt = LocalDateTime.now();

        if (this.readByRecipient == null) {
            this.readByRecipient = false;
        }
    }
}